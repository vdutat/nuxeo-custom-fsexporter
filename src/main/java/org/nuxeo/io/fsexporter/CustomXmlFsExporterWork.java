package org.nuxeo.io.fsexporter;

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.notification.SendMail;
import org.nuxeo.ecm.automation.core.scripting.Scripting;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.TransactionBatchingDocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentTreeReader;
import org.nuxeo.ecm.core.io.impl.plugins.NuxeoArchiveWriter;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class CustomXmlFsExporterWork extends AbstractWork {

    private static final long serialVersionUID = 1L;

    private final static Log LOGGER = LogFactory.getLog(CustomXmlFsExporterWork.class);

    private static final String WORK_NAME = "XML and binaries tree exporter";
    
    public static final String WORK_ID_PREFIX = "CustomXmlFsExporter-";

    private static final String WORK_CATEGORY = "Export";

    private final String docId;

    private final String targetFsFolder;

    private final String initiator;

    private final Integer pageSize;

    private final Boolean batchMode;
    
    public CustomXmlFsExporterWork(String workId, String targetFsFolder, String docId, String initiator, Integer pageSize, Boolean batchMode) {
        super(workId);
        this.docId = docId;
        this.targetFsFolder = targetFsFolder;
        this.initiator = initiator;
        this.pageSize = pageSize;
        this.batchMode = batchMode;
    }
    
    public CustomXmlFsExporterWork(String workId, String targetFsFolder, String docId, String initiator, Integer pageSize) {
        super(workId);
        this.docId = docId;
        this.targetFsFolder = targetFsFolder;
        this.initiator = initiator;
        this.pageSize = pageSize;
        this.batchMode = false;
    }
    
    @Override
    public String getTitle() {
        return WORK_NAME;
    }

    @Override
    public String getCategory() {
        return WORK_CATEGORY;
    }

    @Override
    public void work() throws Exception {
        LOGGER.debug("<work> ");
        DocumentReader reader = null;
        DocumentWriter writer = null;
        initSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));
        File file = File.createTempFile(doc.getName() + "-", ".zip");
        File toFile = new File(targetFsFolder + File.separator + file.getName());
        try {
            Framework.trackFile(file, file);
            setStatus("Temp file created");
            reader = new DocumentTreeReader(session, doc);
            setStatus("Reader created");
            writer = new NuxeoArchiveWriter(file);
            setStatus("Writer created");
            DocumentPipe pipe;
            if (batchMode) {
                pipe = new TransactionBatchingDocumentPipeImpl(pageSize);
            } else {
                pipe = new DocumentPipeImpl(pageSize);
            }
            pipe.setReader(reader);
            pipe.setWriter(writer);
            setStatus("Pipe created");
            LOGGER.debug("Starting export " + getTitle() + " " + getId() + "...");
            pipe.run();
            setStatus("Exported");
            file.renameTo(toFile);
            setStatus("ZIP file created");
        } finally {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Export finished:");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                LOGGER.debug("  Schedule time: " + sdf.format(getSchedulingTime()));
                LOGGER.debug("  Status: " + getStatus());
                LOGGER.debug("  Start time: " + sdf.format(getStartTime()));
                if (getCompletionTime() > 0) {
                    LOGGER.debug("  Completion time: " + sdf.format(getCompletionTime()));
                }
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            new AutomationMail(doc, initiator, getTitle() + " '" + getId() + "' - " + getStatus(), "ZIP file " + toFile.getAbsolutePath() + " created.").send();
        }
    }

    protected class AutomationMail {
        private final DocumentModel doc;
        private final String toUserName;
        private final String subject;
        private final String body;
        private boolean isHtml = false;
        private OperationContext ctx = null;
        
        public AutomationMail(DocumentModel doc, String toUserName, String subject, String body) {
            this.doc = doc;
            this.toUserName = toUserName;
            this.subject = subject;
            this.body = body;
        }

        public AutomationMail html(boolean value) {
            this.isHtml = value;
            return this;
        }

        public AutomationMail context(OperationContext ctx) {
            this.ctx = ctx;
            return this;
        }

        public void send() {
            LOGGER.debug("<send> " + toUserName + ", " + subject + ", " + body);
            try {
                if (ctx == null) {
                    ctx = new OperationContext(doc.getCoreSession());
                }
                ctx.setInput(doc);
                OperationChain chain = new OperationChain("CustomXmlFsExporterSendMail");
                chain.add(SendMail.ID)
                     .set("from", Scripting.newExpression("Env[\"mail.from\"]"))
                     .set("to", Framework.getLocalService(UserManager.class).getPrincipal(toUserName).getEmail())
                     .set("HTML", isHtml)
                     .set("rollbackOnError" , false)
                     .set("subject", subject)
                     .set("message", body);
                Framework.getLocalService(AutomationService.class).run(ctx, chain);
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
            
        }
    }

}
