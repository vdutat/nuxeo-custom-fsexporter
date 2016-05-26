# About
 
* This project provides an automation operation that uses `Core IO` to export a document (and its tree if `Folderish`) to a ZIP file on the server's file system. 
* Project status: prototype
* **NO** Nuxeo Support
 
# Usage

    curl -X POST -u <USERNAME>:<PASSWORD> -H Content-Type:application/json+nxrequest -d '{ "params": {"targetFolderFullPath":"<TARGET_FS_FOLDER_FULLPATH_HERE>"}}' http://localhost:8080/nuxeo/api/v1/repo/default/path/<SOURCE_NUXEO_DOCUMENT_PATH_HERE>/@op/CustomXmlFsExporter

# Code
## Content
 
Operation `CustomXmlFsExporter` accepts the following parameters:
- `targetFolderFullPath`: full path of folder on server's file system where the ZIP file will be created
- `pageSize`: number of documents retrieved per page (optional, default: 100)
- `batchMode`: enables batch mode. Transactions will be committed every `pageSize` processed documents (optional, default: false)
An email is sent to the initiator user when the export is finished.

## Build
 
    mvn clean install
 
## Deploy (how to install build product)
 
Copy generated JAR file in `$NUXEO_HOME/nxserver/bundles/` or `$NUXEO_HOME/nxserver/plugins`

# About Nuxeo
 
The [Nuxeo Platform](http://www.nuxeo.com/products/content-management-platform/) is an open source customizable and extensible content management platform for building business applications. It provides the foundation for developing [document management](http://www.nuxeo.com/solutions/document-management/), [digital asset management](http://www.nuxeo.com/solutions/digital-asset-management/), [case management application](http://www.nuxeo.com/solutions/case-management/) and [knowledge management](http://www.nuxeo.com/solutions/advanced-knowledge-base/). You can easily add features using ready-to-use addons or by extending the platform using its extension point system.
 
The Nuxeo Platform is developed and supported by Nuxeo, with contributions from the community.
 
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with
SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.
More information is available at [www.nuxeo.com](http://www.nuxeo.com).
