#!/usr/bin/env bash
echo "begin install..."
sh ./uninstall.sh
read -p "Do you want to automatically install dependencies (JDK, maven, node) ? (Press y|Y for Yes, any other key for No). " install_depency
if [ "$install_depency" = "Y" -o "$install_depency" = "y" ]; then
  sh ./install-depency.sh
else
  echo "Please install the dependencies required by the system by yourself, including openjdk (11), maven (3), and node (16.15.1)."
  exit 1
fi


read -p "Please enter the host of openGauss.: " host
if [ ! -n "$host" ]; then
  echo "Host cannot be empty."
  exit 1
fi
read -p "Please enter the port of openGauss.: " port
if [ ! -n "$port" ]; then
  echo "Port cannot be empty."
  exit 1
fi
read -p "Please enter the username of openGauss.: " username
if [ ! -n "$username" ]; then
  echo "Username cannot be empty."
  exit 1
fi
read -p "Please enter the password of openGauss.: " password
if [ ! -n "$password" ]; then
  echo "Password cannot be empty."
  exit 1
fi
read -p "Please enter the database of openGauss.: " database
if [ ! -n "$database" ]; then
  echo "Database cannot be empty."
  exit 1
fi

echo "host: $host, port: $port  username: $username password: $password database: $database"
cp config/application-temp.yml config/application-cus.yml
sed -i "18s/ip:port/$host:$port/" config/application-cus.yml
sed -i "18s/database/$database/" config/application-cus.yml
sed -i "19s/dbuser/$username/" config/application-cus.yml
sed -i "20s/dbpassword/$password/" config/application-cus.yml

mvn clean install -P prod -Dmaven.test.skip=true
mkdir -p /ops/server/openGauss-visualtool/
mkdir -p /ops/files/
mkdir -p /ops/server/openGauss-visualtool/logs/
mkdir -p /ops/server/openGauss-visualtool/config/
touch /ops/server/openGauss-visualtool/logs/visualtool-main.out
cp visualtool-api/target/visualtool-main.jar /ops/server/openGauss-visualtool/
mv config/application-cus.yml /ops/server/openGauss-visualtool/config/
chown -R og_ops:og_ops /ops
echo "end install"
