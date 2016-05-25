/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     vdutat
 */

package org.nuxeo.io.fsexporter.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.io.fsexporter.CustomXmlFsExporterWork;
import org.nuxeo.runtime.api.Framework;

/**
 *
 */
@Operation(id=CustomXmlFsExporterOperation.ID, category=Constants.CAT_SERVICES, label="Custom Xml Fs Exporter", description="")
public class CustomXmlFsExporterOperation {

    public static final String ID = "CustomXmlFsExporter";

    private final static Log LOGGER = LogFactory.getLog(CustomXmlFsExporterOperation.class);

    @Param(name = "targetFolderFullPath", description = "Target folder's full path", required = true)
    protected String targetFolderFullPath;

    @Param(name = "pageSize", description = "Page Size (default: 100)", required = false)
    protected Integer pageSize = 100;

    @Param(name = "batchMode", description = "Batch mode", required = false)
    protected Boolean batchMode = false;

    @Context
    protected OperationContext ctx;

    @Context
    protected UserManager um;

    @OperationMethod
    public DocumentModel run(DocumentModel input) {
        if (um.getPrincipal(ctx.getPrincipal().getName()).isAdministrator()) {
            Work work = new CustomXmlFsExporterWork(CustomXmlFsExporterWork.WORK_ID_PREFIX + input.getId(), targetFolderFullPath, input.getId(), ctx.getPrincipal().getName(), 500, true);
            WorkManager workManager = Framework.getLocalService(WorkManager.class);
            workManager.schedule(work, true);
            LOGGER.info("Queue ID: " + workManager.getCategoryQueueId(work.getCategory()));
        } else {
            LOGGER.error("insufficient rights to launch export");
        }
        return input;
    }

}
