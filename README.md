# A Federated Learning Framework baesd on Zookeeper, Netty with Pytorch
一个联邦学习程序，服务器负责从客户端收集模型并聚合，然后下发。客户端接收模型并进行联邦学习训练。
请自定义Learning/train.py模型训练文件
服务器配置文件：
fl:
  type: server # server->联邦学习服务端； client->联邦学习客户端
  port: 5181 # 端口号
  number: 2 # 若为联邦学习服务端，number->客户端数量；否则取任意值
  客户端配置文件
fl:
  type: client # server->联邦学习服务端； client->联邦学习客户端
