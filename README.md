# [Autoconfig VRPOD](https://doi.org/XXXXX)

## Abstract
VRPOD 

## Authors
Authors involved in this work and their respective contributions:
- Raúl Martín Santamaría
- Manuel López-Ibáñez
- Thomas Stützle
- Jose Manuel Colmenar Verdugo

## Datasets

Instances are categorized in different datasets inside the 'instances' folder.

## Compiling

You can easily compile and build an executable artifact of this project using Maven and a recent version of Java (17+):
```text
mvn clean package
```

## Executing

You can just run the generated jar file in target. For easy of use there is an already compiled JAR inside the target folder.
To review a full list of configurable parameters, either using an application.yml in the same folder as the executable, or using command line parameters, see the Mork documentation, section configuration.
Example: execute the IteratedGreedyExperiment using a new set of instances located inside the `newinstances` folder.

```text
java -jar target/VRPOD.jar --instances.path.default=instances
```

## Cite

Consider citing our paper if used in your own work:
PENDING PUBLICATION

### DOI
https://doi.org/XXXXXXX

### Bibtex
```bibtex
@article{
...
}
```
