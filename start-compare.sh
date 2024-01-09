set -e
mvn clean package
java \
-Xmx24G \
-Xms24G \
-jar target/VRPOD-0.19-SNAPSHOT.jar \
--serializers.solution-json.enabled=true \
--serializers.solution-json.frequency=all \
--solver.experiments=FinalExperiment \
--solver.parallelExecutor=true \
--solver.nWorkers=8 \
--solver.metrics=true
