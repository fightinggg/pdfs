FROM maven:3.8.2-openjdk-16
WORKDIR /app
#COPY pom.xml /app/pom.xml
#RUN mvn dependency:go-offline
COPY . /app
RUN mvn package -Dmaven.test.skip

FROM centos:centos8
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-* \
    && sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-* \
    && curl https://mirrors.aliyun.com/repo/Centos-vault-8.5.2111.repo -L -o /etc/yum.repos.d/CentOS-Base.repo \
    && yum clean all \
    && yum makecache
RUN yum install git java -y
COPY --from=0 /app/target/pdfs-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app/pdfs.jar
CMD java -jar /app/pdfs.jar
