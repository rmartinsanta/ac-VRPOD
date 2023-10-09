set -e
mvn clean package
java -jar target/VRPOD-0.18-SNAPSHOT.jar --instance-selector \
--instances.preliminar-output-path=instances/tuning \
--instances.for-selection=instances/all \
--instances.preliminar-percentage=0.05
