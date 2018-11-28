# RISCV-Simulator
An instruction set simulator for the RISC-V architecture written in Java.
Written as the [last assignment](https://github.com/schoeberl/cae-lab/tree/master/finasgmt) for the course "02155: Computer Architecture and Engineering" at the Technical University of Denmark

Simulates the [RV32I Base Instruction Set](https://content.riscv.org/wp-content/uploads/2017/05/riscv-spec-v2.2.pdf) (excluding EBREAK, CSR*, fence* and some environment calls)

# Environment Calls
| ID `x10`    | Name         | Description                                            |
|-------------|--------------| -------------------------------------------------------|
| 1           | print_int    | Prints integer in `x11`                                |
| 4           | print_string | Prints null-terminated string whose address is in `x11`|
| 10          | exit         | Stops execution                                        |
| 11          | print_char   | Prints character in `x11`                              |

# Compiling and running
## Install packages
If you haven't run a JavaFX application on Ubuntu before run the following command: 
```
sudo apt-get install openjfx
```

## Java Development Kit 8
### Compile
Assuming no other Java files present:
```
cd path/to/package/files
javac *.java
```
### Run
Assuming current work directory contains RISCVSimulator package directory:
```
cd path/to/package/
java RISCVSimulator.Main
```
## OpenJDK 11
As OpenJDK no longer supplies a runtime environment or JavaFX, it is required to have [OpenJFX](https://openjfx.io/) downloaded.
The path to OpenJFX will be referred to as `%PATH_TO_FX%`.
### Compile
```
cd path/to/package/files
javac --module-path %PATH_TO_FX% --add-modules javafx.fxml,javafx.base,javafx.controls,javafx.graphics *.java
```

### Run
Requires a Java 11 Runtime Environment. This is easily obtained on Ubuntu through apt, but Windows users will need to use `jlink` to build their own. See `Releases` for example.
Assuming current work directory contains RISCVSimulator package directory:
```
cd path/to/package
java --module-path %PATH_TO_FX% --add-modules javafx.fxml,javafx.base,javafx.controls,javafx.graphics RISCVSimulator.Main
```

Unfortunately, the program was not written with modular Java support in mind. For this reason, there is no better way of running the program, as it's not possible to use `jlink` in order to build the application with all dependencies bundled. Writing batch files or shell scripts is adviced.
