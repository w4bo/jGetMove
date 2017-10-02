[latest release]:(https://github.com/jGetMove/jGetMove/releases/latest)
# jGetMove v2.0.1 - Xénophon

GetMove algorithm in java

## Sources

To compile the project, simply use `make`. It will create `jGetMove.jar`.

You can also build it manually 
```bash
$ mkdir -p out/
$ javac -extdirs lib/ -sourcepath src/ src/fr/jgetmove/jgetmove/Main.java -d out/
$ jar cvfm jGetMove.jar Manifest.mf -C out/ . lib/
```

or download it here: [Latest Release](https://github.com/jGetMove/jGetMove/releases/latest)

## Usage 

To execute the program (e.g. to see the usage): 
```bash
$ java -jar jGetMove.jar --help
```


## Input files

### Transaction-Clusters

The transaction cluster association is defined in this file (named `index.dat` by conventions).
the `Transaction` is the line number (begginning by `0`) and it iterates through the `Cluster`s written on it's line.

#### Example

> In this example there are two transactions (`0` and `1`) which respectively iterate through `[0,1,2]` and `[2,3,4]`.
> Following this logic there are 5 clusters (from `0` to `4`).
>
> **Note :** the javadoc refers to this structure with : `transactionId [clusterId ...]`

```
0 1 2
2 3 4
```
> A working example is present in `assets/example.dat`

### Cluster-Time

The relation between the cluster and the Time are defined here (named `time_index.dat` by conventions).

Each line of the file is a link between the `Time` and the `Cluster` by their id.

#### Example

> The following file is the cluster-time link of cluster-transaction file
> each line has a `Time` (beginning at `1`) and the clusters.
> The first and the second `Time` have two clusters.
> Please note that the cluster **NEED TO BE** present in `index.dat`, an exception will be returned otherwise. 

```
1 0
1 1
2 2
2 3
3 4
```
> A working example can be found in `assets/example_time_index.dat`
