terraform {
  required_providers {
    aws = {
      source ="hashicorp/aws"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_vpc" "vpc_1" {
  cidr_block = "10.0.0.0/16"

  enable_dns_support = true
  enable_dns_hostnames = true

}

resource "aws_subnet" "sub1" {
  vpc_id = aws_vpc.vpc_1.id
  cidr_block = "10.0.1.0/24"
  availability_zone = "ap-northeast-2a"
}

resource "aws_subnet" "sub2" {
  vpc_id = aws_vpc.vpc_1.id
  cidr_block = "10.0.2.0/24"
  availability_zone = "ap-northeast-2b"
}
resource "aws_subnet" "sub3" {
  vpc_id = aws_vpc.vpc_1.id
  cidr_block = "10.0.3.0/24"
  availability_zone = "ap-northeast-2c"
}
resource "aws_subnet" "sub4" {
  vpc_id = aws_vpc.vpc_1.id
  cidr_block = "10.0.4.0/24"
  availability_zone = "ap-northeast-2d"
}

resource "aws_internet_gateway" "gateway1" {
  vpc_id = aws_vpc.vpc_1.id
}

resource "aws_route_table" "route_table1" {
  vpc_id = aws_vpc.vpc_1.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gateway1.id
  }
}

resource "aws_route_table_association" "association1" {
  subnet_id = aws_subnet.sub1.id
  route_table_id = aws_route_table.route_table1.id
}
resource "aws_route_table_association" "association2" {
  subnet_id = aws_subnet.sub2.id
  route_table_id = aws_route_table.route_table1.id
}
resource "aws_route_table_association" "association3" {
  subnet_id = aws_subnet.sub3.id
  route_table_id = aws_route_table.route_table1.id
}
resource "aws_route_table_association" "association4" {
  subnet_id = aws_subnet.sub4.id
  route_table_id = aws_route_table.route_table1.id
}

resource "aws_security_group" "sg1" {
  vpc_id = aws_vpc.vpc_1.id
  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    protocol = "all"
    from_port =0
    to_port = 0
  }
  egress {
    cidr_blocks = ["0.0.0.0/0"]
    protocol = "all"
    from_port =0
    to_port = 0
  }
}

resource "aws_iam_role" "ec2_role_1" {
  name = "app3-ec2-role-1"

  # 이 역할에 대한 신뢰 정책 설정. EC2 서비스가 이 역할을 가정할 수 있도록 설정
  assume_role_policy = <<EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "",
        "Action": "sts:AssumeRole",
        "Principal": {
            "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow"
      }
    ]
  }
  EOF
}

# EC2 역할에 AmazonS3FullAccess 정책을 부착
resource "aws_iam_role_policy_attachment" "s3_full_access" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

# EC2 역할에 AmazonEC2RoleforSSM 정책을 부착
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM"
}

# IAM 인스턴스 프로파일 생성
resource "aws_iam_instance_profile" "instance_profile_1" {
  name = "app3-instance-profile-1"
  role = aws_iam_role.ec2_role_1.name
}

locals {
  ec2_user_data_base = <<-END_OF_FILE
#!/bin/bash
# 가상 메모리 4GB 설정
sudo dd if=/dev/zero of=/swapfile bs=128M count=32
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
sudo sh -c 'echo "/swapfile swap swap defaults 0 0" >> /etc/fstab'

# 도커 설치 및 실행/활성화
yum install docker -y
systemctl enable docker
systemctl start docker

# 도커 네트워크 생성
docker network create common

# nginx 설치
docker run -d \
  --name npm_1 \
  --restart unless-stopped \
  --network common \
  -p 80:80 \
  -p 443:443 \
  -p 81:81 \
  -e TZ=Asia/Seoul \
  -v /dockerProjects/npm_1/volumes/data:/data \
  -v /dockerProjects/npm_1/volumes/etc/letsencrypt:/etc/letsencrypt \
  jc21/nginx-proxy-manager:latest

# redis 설치
docker run -d \
  --name=redis_1 \
  --restart unless-stopped \
  --network common \
  -p 6379:6379 \
  -e TZ=Asia/Seoul \
  redis --requirepass lldj123414

# mysql 설치
docker run -d \
  --name mysql_1 \
  --restart unless-stopped \
  -v /dockerProjects/mysql_1/volumes/var/lib/mysql:/var/lib/mysql \
  -v /dockerProjects/mysql_1/volumes/etc/mysql/conf.d:/etc/mysql/conf.d \
  --network common \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=lldj123414 \
  -e TZ=Asia/Seoul \
  mysql:latest

# MySQL 컨테이너가 준비될 때까지 대기
echo "MySQL이 기동될 때까지 대기 중..."
until docker exec mysql_1 mysql -uroot -plldj123414 -e "SELECT 1" &> /dev/null; do
  echo "MySQL이 아직 준비되지 않음. 5초 후 재시도..."
  sleep 5
done
echo "MySQL이 준비됨. 초기화 스크립트 실행 중..."

docker exec mysql_1 mysql -uroot -plldj123414 -e "
CREATE USER 'lldjlocal'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '1234';
CREATE USER 'lldjlocal'@'172.18.%.%' IDENTIFIED WITH caching_sha2_password BY '1234';
CREATE USER 'lldj'@'%' IDENTIFIED WITH caching_sha2_password BY 'lldj123414';

GRANT ALL PRIVILEGES ON *.* TO 'lldjlocal'@'127.0.0.1';
GRANT ALL PRIVILEGES ON *.* TO 'lldjlocal'@'172.18.%.%';
GRANT ALL PRIVILEGES ON *.* TO 'lldj'@'%';

CREATE DATABASE glog_prod;

FLUSH PRIVILEGES;
"

echo "${var.github_access_token_1}" | docker login ghcr.io -u ${var.github_access_token_1_owner} --password-stdin

END_OF_FILE
}

resource "aws_instance" "ec2-1" {
  ami = "ami-0eb302fcc77c2f8bd"
  instance_type = "t3.micro"
  subnet_id = aws_subnet.sub2.id
  vpc_security_group_ids = [aws_security_group.sg1.id]
  associate_public_ip_address = true
  iam_instance_profile = aws_iam_instance_profile.instance_profile_1.name
  tags = {
    Name = "dev-ec2-1"
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 12
  }
  user_data = <<-EOF
${local.ec2_user_data_base}
EOF
}