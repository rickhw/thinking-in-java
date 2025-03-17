export SPRING_DATA_MONGODB_URI=mongodb://172.31.5.148:27017/school
export SPRING_DATA_REDIS_HOST=172.31.5.148
java -jar target/school-0.0.1-SNAPSHOT.jar

SPRING_DATA_MONGODB_URI=mongodb://192.168.2.65:27017/school SPRING_DATA_REDIS_HOST=192.168.2.65 java -jar target/school-0.0.1-SNAPSHOT.jar
