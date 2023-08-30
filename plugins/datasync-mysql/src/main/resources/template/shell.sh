#!/usr/bin/env bash
echo "init env"
sudo /usr/bin/python3 -m venv venv
source venv/bin/activate
sudo /usr/bin/python3 setup.py install
echo "init config"
sudo pip3 install dataclasses
chameleon set_configuration_files
cp /home/$USER/.pg_chameleon/configuration/config-example.yml /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '72,98d' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '23s/localhost/#{[ogHost]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '24s/5432/#{[ogPort]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '25s/usr_replica/#{[ogUser]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '26s/never_commit_password/#{[ogPass]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '27s/db_replica/#{[ogDatabase]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '35s/localhost/#{[mysqlHost]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '36s/3306/#{[mysqlPort]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '37s/usr_replica/#{[mysqlUser]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '38s/never_commit_passwords/#{[mysqlPass]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '42s/delphis_mediterranea/#{[mysqlSchema]}/' /home/$USER/.pg_chameleon/configuration/default.yml
sed -i '42s/loxodonta_africana/#{[ogSchema]}/' /home/$USER/.pg_chameleon/configuration/default.yml
echo "staring sync"
chameleon create_replica_schema --config default
chameleon add_source --config default --source mysql
chameleon init_replica --config default --source mysql
echo "sync complete"