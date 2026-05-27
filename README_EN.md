# openGauss DataKit
The installation and O&M of openGauss can be technically challenging and complex for beginners or users who simply want to test basic database features. In particular, the installation of the Enterprise Edition presents a steep learning curve for many users. The visualized O&M platform abstracts the technical details of openGauss, enabling common users to quickly experience its functions and allowing O&M personnel to rapidly deploy or uninstall various openGauss clusters in enterprise environments. This reduces learning and O&M costs, visualizes common openGauss operations, and eliminates the command differences between various openGauss versions. Consequently, users can operate the database consistently without knowing low-level command details, allowing them to focus on their core business logic.

To support customers with diverse configurations and O&M requirements, targeted monitoring and management tools are necessary. The community requires an integrated platform to consolidate these tools as plug-ins while supporting flexible, personalized configurations.

This project is a web-based visualization platform for openGauss. It aims to lower the barrier for installing and using openGauss by providing centralized security management, plug-in management, and other features, including one-click deployment and uninstallation, component-based installation, multi-version upgrades, routine O&M, and monitoring.


## Project Repository Structure
```
├── openGauss-datakit                      // Platform project
├── openGauss-datakit-agent                // Platform agent project
├── plugins
├───├─alert-monitor                        // Alarm monitoring plug-in
├───├─base-ops                             // Basic O&M plug-in
├───├─compatibility-assessment             // Compatibility assessment plug-in
├───├─container-management-plugin          // Container management plug-in
├───├─data-migration                       // Data migration plug-in
├───├─data-studio                          // Service development plug-in, web-based DataStudio
├───├─observability-instance               // Intelligent O&M plug-in: Instance monitoring
├───├─observability-log-search             // Intelligent O&M plug-in: Log search
├───├─observability-sql-diagnosis          // Intelligent O&M plug-in: Slow SQL diagnosis
```

## Download Links

##### DataKit Download Links

| Package Name                           | Remarks                                                       | Download Link                                                                                                          |
|:------------------------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| Datakit-All-7.0.0-RC3.tar.gz  | Full package (includes all plug-ins)                                              | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/openGauss-Datakit-All-7.0.0-RC3.tar.gz |
| Datakit-Mini-7.0.0-RC3.tar.gz | (Recommended) Minimal package (Includes only basic plug-ins: [Business Development] and [Basic O&M]. Additional plug-ins can be downloaded on the `Plug-in Management` page.)| https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/openGauss-Datakit-Mini-7.0.0-RC3.tar.gz          |

#### Note:
1. For versions prior to 7.0.0-RC2, all plug-ins are included by default. The package naming convention is `Datakit-_$VERSION_.tar.gz`, for example, version 6.0.0 is named `Datakit-6.0.0.tar.gz`.
2. For version 7.0.0-RC2 and later, use the links in the table above to choose between the full and minimal packages.
3. If you use the minimal package, plug-ins can be downloaded later from the `Plug-in Management` page in DataKit using either online or offline methods.
4. Online download: Select the required plug-in from the drop-down list and confirm your selection.
5. Offline download: Visit the `openGauss Tools` section on the [official download page](https://opengauss.org/en/download/), select the specific plug-in JAR files from the `Datakit_Mini_7.0.0-RC3` drop-down list. Verify the file integrity by comparing the provided SHA256 checksum with the calculated value of your downloaded file. If they match, the file is complete; otherwise, re-download. Upload the verified JAR package to DataKit.
6. For offline installation, the version of the uploaded plug-in must match the version of the deployed DataKit.

##### Plug-in Download Links

| Package Name                                                 | Remarks         | Download Link                                                                                                                                        |
|:----------------------------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| alert-monitor-7.0.0-RC3-repackage.jar               | Alarm monitoring plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/alert-monitor-7.0.0-RC3-repackage.jar                                         |
| data-migration-7.0.0-RC3-repackage.jar              | Data migration plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/data-migration-7.0.0-RC3-repackage.jar             |
| webds-plugin-7.0.0-RC3-repackage.jar                | Service development plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/webds-plugin-7.0.0-RC3-repackage.jar               |
| base-ops-7.0.0-RC3-repackage.jar                    | Basic O&M plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/base-ops-7.0.0-RC3-repackage.jar                   |
| observability-instance-7.0.0-RC3-repackage.jar      | Instance monitoring plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-instance-7.0.0-RC3-repackage.jar     |
| observability-log-search-7.0.0-RC3-repackage.jar    | Log search plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-log-search-7.0.0-RC3-repackage.jar   |
| compatibility-assessment-7.0.0-RC3-repackage.jar    | Compatibility evaluation tool   | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/compatibility-assessment-7.0.0-RC3-repackage.jar |
| observability-sql-diagnosis-7.0.0-RC3-repackage.jar | Intelligent diagnosis plug-in     | https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/Datakit/visualtool-plugin/observability-sql-diagnosis-7.0.0-RC3-repackage.jar                  |

     
## Downloading the Release Version

https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC1/tools/Datakit/Datakit-7.0.0-RC1.tar.gz

https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC2/tools/Datakit/Datakit-All-7.0.0-RC2.tar.gz
https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.0-RC2/tools/Datakit/Datakit-Mini-7.0.0-RC2.tar.gz

To ensure the software package is not corrupted during transmission, for example, due to network fluctuations or storage media issues, you must verify its integrity. Only verified packages can be deployed. Follow the steps below to proceed:

1. Calculate the SHA256 value of the downloaded package (using `Datakit 7.0.0-RC1` as an example. The operations for other versions are the same.)

~~~
sha256sum Datakit-7.0.0-RC1.tar.gz
~~~

2. Visit the `openGauss Tools` section on the [official download page](https://opengauss.org/en/download/). Verify the file integrity by comparing the provided SHA256 checksum with the calculated value of your downloaded file. If they match, the file is complete; otherwise, re-download.

## Note
1. Plug-ins must run on the platform. Therefore, you must install and deploy the platform project before packaging and installing additional plug-in projects.

2. The plug-in development scaffolding is provided to help developers quickly build plug-ins compatible with the platform. Developers can build business logic on top of this scaffolding. The dependency versions in the scaffolding have been verified for optimal compatibility; therefore, it is highly recommended that you do not modify these versions.

## Compiling the Code
1. Ensure Java 17+, Maven 3.9.0+, and Node.js v18+ (including npm) are installed, and that Maven and Node.js mirrors and registries are properly configured.
2. Locally compile and install the SpringBrick component from: https://gitcode.com/wang4721/springboot-plugin-framework-parent.git
3. Download the DataKit code and execute `sh build.sh`.
4. Compiled artifacts will be located in the `output` directory.

## Installation Procedure
1. Decompress the installation package.
   Obtain the installation package `Datakit-All-7.0.0-RC3.tar.gz` or `Datakit-Mini-7.0.0-RC3.tar.gz` with the download links or by compiling the source code. If you do not require all plug-ins, the `Datakit-Mini-7.0.0-RC3.tar.gz` package is recommended. You can add plug-ins as needed through the management interface later.)
   Decompress the package into your chosen installation directory. For example, if your installation path is `/path/datakit_server`, run the following commands:
   ```shell
   $ tar -zxvf Datakit-All-7.0.0-RC3.tar.gz -C /path/to/datakit_server
   ./agent/
   ./agent/application.yml
   ./agent/datakit-agent-7.0.0-RC3-runner.jar
   ./application-temp.yml
   ./build_commit_id.log
   ./doc/
   ./doc/data-migration-README.md
   ./doc/base-ops-README.md
   ./doc/oauth-login-README.md
   ./doc/observability-log-search-README.md
   ./doc/compatibility-assessment-README.md
   ./doc/observability-sql-diagnosis-README.md
   ./doc/observability-instance-README.md
   ./doc/intelligent-parameter-tuning-README.md
   ./doc/openGauss-tools-monitor-README.md
   ./doc/alert-monitor-README.md
   ./doc/data-studio-README.md
   ./doc/datakit-README.md
   ./openGauss-datakit-7.0.0-RC3.jar
   ./run.sh
   ./visualtool-plugin/
   ./visualtool-plugin/observability-sql-diagnosis-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/compatibility-assessment-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/alert-monitor-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/webds-plugin-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/observability-log-search-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/base-ops-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/data-migration-7.0.0-RC3-repackage.jar
   ./visualtool-plugin/observability-instance-7.0.0-RC3-repackage.jar
   ```
2. Run the installation script.

   Switch to the DataKit installation directory and execute the `install.sh` script. The script will automatically create the directory structure required for the DataKit service and update the corresponding paths in the configuration files.

   ```shell
   sh install.sh
   ```

   The execution log is as follows:

   ```
   Checking the DataKit jar...
   Check the DataKit jar is openGauss-datakit-7.0.0-RC3.jar
   Check the DataKit jar successfully
   Creating required directories...
   Create required directories successfully
   Checking Java version...
   Check Java version is 17.0.13
   Generating the SSL key...
   Generating 4,096 bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 365 days
           for: CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, ST=Beijing, C=CN
   Generate the SSL key successfully
   Configuring the application-temp.yml...
   Configuring the application-temp.yml successfully
   Datakit has been installed successfully.
   Please go to the '/path/datakit_server' directory to manually start the DataKit server.
   The command is as follows:
       sh ./run.sh start --aes-key ******
       sh ./run.sh status
       sh ./run.sh stop
       sh ./run.sh restart --aes-key ******
   ```

3. Startup and routine O&M

   After successful installation, use the following `run.sh` script commands to start, stop, and manage the DataKit service.

   The `--aes-key` parameter in the startup script is the DataKit startup password used for internal encryption and decryption. DataKit does not store this password, and it cannot be modified. You must keep this password secure. It must remain consistent across multiple restarts. Otherwise, DataKit will fail to start.

   Start the application:
   ```shell
   sh ./run.sh start --aes-key xxxxxx
   ```
   Stop the application:
   ```shell
   sh ./run.sh stop
   ```
   Restart the application:
   ```shell
   sh ./run.sh restart --aes-key xxxxxx
   ```
   Check the application status:
   ```shell
   sh ./run.sh status
   ```
4. Accessing the Service

   Once started, access the service via a browser at `https://ip:9494/`. In the URL, replace `ip` with the host IP where DataKit is installed, and `9494` is the default port (replace if modified). The initial username is `admin` and the initial password is `admin123`. You must change the initial password upon the first login.

## Supplement: Switching the DataKit Backend Database

   By default, DataKit uses the `Intarkdb` built-in database to store DataKit operational data. To switch the backend database to `openGauss`, follow these steps:

### Enabling the openGauss Database
   
   Edit the `config/application-temp.yml` file in the DataKit installation directory. Comment out the `driver-class-name` and `url` items for `Intarkdb`, then uncomment the `openGauss` configuration block and provide the correct `jdbc` connection details.
   
   **Note**: When using `openGauss` as the backend database, it must support remote connections, and the configured user must have `sysadmin` permissions. For remote connection and user permission configuration, refer to the section **Supplement: openGauss Parameter Configuration**.

   ```yml
   # For openGauss
   driver-class-name: org.opengauss.Driver
   url: jdbc:opengauss://ip:port/database?currentSchema=public&batchMode=off
   username: dbuser
   password: ******
   ```

### Enabling the Intarkdb Database

   Edit the `config/application-temp.yml` file in the DataKit installation directory. Comment out the `driver-class-name`, `url`, `username`, and `password` items for `openGauss`, then uncomment the following `Intarkdb` configuration block.
   
   ```yml
   # For Intarkdb
   driver-class-name: org.intarkdb.Driver
   url: jdbc:intarkdb:data/datakit
   ```

## Supplement: openGauss Parameter Configuration
1. Install openGauss.
   Refer to the official tutorials for downloading and installing the `openGauss` database. Download address: https://opengauss.org/en/download/
2. Switch to the database installation user and load environment variables.
   After installation, switch to the database installation user, for example, `omm`. Then, `source` the environment variable file, for example, `~/.bashrc` to load the `openGauss` environment. Replace `~/.bashrc` with the actual file path used in your environment.
   ```shell
   source ~/.bashrc
   ```
3. Configure parameters (enabling remote connections).
   Run the following command to modify the `pg_hba.conf` parameters. If using IPv6, replace "0.0.0.0/0" with "::/0"):
   ```shell
   gs_guc set -D /opt/software/openGauss/data/single_node -h "host all all 0.0.0.0/0 sha256"
   ```
   Run the following command to modify the `postgresql.conf` parameters:
   ```shell
   gs_guc set -D /opt/software/openGauss/data/single_node -c "listen_addresses = '*'"
   ```
   In the commands above, `/opt/software/openGauss/data/single_node` is the database node's installation directory. Replace it with your actual path. This directory contains the preceding two files. These configurations allow the database to accept connection requests from any IP address so that external servers can connect to the database.
4. Restart the database to apply the changes. Replace `/opt/software/openGauss/data/single_node` with your actual path according to the method in step 3.
   ```shell
   gs_ctl restart -D /opt/software/openGauss/data/single_node
   ```
5. Connect to the database.
   Run the following command to connect to the database. In the command, `5432` is the default port of the `openGauss` database. Replace it with your actual port.
   ```shell
   gsql -d postgres -p 5432 -r
   ```
6. Create a user and database. Grant the `sysadmin` permission to the user.
   Once connected, run the following three commands to create a user, grant administrative permissions, and create the database:
   ```shell
   create user opengauss_test with password 'Sample@123';
   grant all privileges to opengauss_test;
   create database db_datakit;
   ```
   The `openGauss` database does not support remote connection using the initial user. Therefore, you need to create a user for `datakit` to remotely connect to the database. In addition, `datakit` requires administrator permissions to perform operations on the database. Therefore, you need to grant the administrator permission to the user. The `db_datakit` database is created as the underlying database of the `datakit` platform. After the `datakit` is successfully connected, the data is automatically initialized.
7. After all configurations are complete, keep the `openGauss` database service running.

## Supplementary: IntarkDB Usage Description
### 1. Use the source code to compile and run `IntarkDB`. 
   The compilation and execution of the `IntarkDB` database can be referenced at the [openGauss-embedded](https://gitee.com/opengauss/openGauss-embedded/tree/master) repository.
### 2. Use `IntarkDB-JDBC` for local connection to creation of `IntarkDB`. 
   In Java projects, you can use `IntarkDB-JDBC` to directly create and connect to `IntarkDB`. The corresponding JAR package is placed in the `lib` directory. 
#### Connection URL format
   ```jdbc:intarkdb:{Database path}```
   
   For example, to connect to the database in the `data/datakit`: `jdbc:intarkdb:data/datakit` 

   If the database in the target path does not exist, `IntarkDB-JDBC` will automatically create the database before establishing the connection.
### 3. Use `IntarkDB-JDBC` to enable `IntarkDB` network services. 
   Connect to or create an `IntarkDB` while simultaneously enabling network services. This allows other programs to connect to the `IntarkDB` database over the network.   
#### Connection URL format  
   ```jdbc:intarkdb:{Database directory}?open_remote=true&port={}&dbname={}``` 

   For example, to enable port 9000 and set the database name to `intarkdb`: ```jdbc:intarkdb:data/datakit?open_remote=true&port=9000&dbname=intarkdb``` 
### 4. Use `IntarkDB-JDBC` to connect to `IntarkDB` over the network. 
   When the network service is enabled on an `IntarkDB` instance, you can connect to that remote instance over the network.
#### Connection URL format
   ```jdbc:intarkdb:tcp://{ip:port}?dbname={dbname}```  
   
   For example, to connect to the intarkdb database at 127.0.0.1:9000: `jdbc:intarkdb:tcp://127.0.0.1:9000?dbname=intarkdb`
   

## Participating in Development
For guidance on setting up your environment, see [Setting Up the Development Environment](https://gitee.com/opengauss/openGauss-workbench/tree/master/openGauss-datakit/doc/DataKit%20Dev%20Setup.md).

For plug-in development, please refer to the development manual located in the `openGauss-datakit/doc` directory.

When adding a new plug-in, it is essential to update the `build.sh` script to ensure that the project can still be compiled through the one-click build process.
