-- 作用：初始化本地开发 MySQL 的远程账号，便于应用、Canal 和可视化工具从宿主机或容器网络连接。
ALTER USER 'root'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'canal'@'%' IDENTIFIED BY 'canal';
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;
