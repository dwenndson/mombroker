# Mombroker

Projeto consistem em criar um projeto de mensageria onde pode-se criar tópicos publicos e tópicos pessoais, tecnologia utilizar para kafka para criação de topicos e envio de mensagens;

### Ponto de atenção
 Necessário ter instalado na maquina o docker, antes de inicializar o projeto necessário na pasta raiz do projeto da o seguinte comedo.
 
```` 
docker-compose up 
````

Iniciará o processo de donwload do kafka e zookeeper.
Verifique se os container estão funcionando 

![img.png](img.png)

Na pasta raiz do projeto novamente execute o comando.

````
mvn clean install
````


Após subir os container iniciar o código com o comando.
````
 mvn spring-boot:run
````