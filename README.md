# RISCV-Simulator
An instruction set simulator for the RISC-V architecture written in Java.
Written as the [last assignment](https://github.com/schoeberl/cae-lab/tree/master/finasgmt) for the course "02155: Computer Architecture and Engineering" at the Technical University of Denmark

Simulates the [RV32I Base Instruction Set](https://content.riscv.org/wp-content/uploads/2017/05/riscv-spec-v2.2.pdf) (excluding EBREAK, CSR* and fence*)

# Environment Calls
| ID `x10` | Name | Description |
|-------------|-------------| -----|
| 1     | print_int | Prints integer in `x11` |
| 4      | print_string | Prints null-terminated string whose address is in `x11`|
| 10 | exit | Stops execution |
| 11 | print_char | Prints character in `x11` |

# Compiling and running
## Java Development Kit 8/9
### Compile
Assuming no other Java files present:
```
javac path/to/files/*.java
```
### Run
Assuming current work directory contains RISCVSimulator package directory:
```
java RISCVSimulator.Main
```
## OpenJDK 10+
As OpenJDK no longer supplies a runtime environment or JavaFX, it is required to have [OpenJFX](https://openjfx.io/) downloaded.
The path to OpenJFX will be referred to as `%PATH_TO_FX%`.
### Compile
```
javac --module-path %PATH_TO_FX% --add-modules javafx.fxml,javafx.base,javafx.controls,javafx.graphics path/to/files/*.java
```

### Run
Requires a Java 11 Runtime Environment. This is easily obtained on Ubuntu through apt, but Windows users will need to use `jlink` to build their own. See `Releases` for example.
Assuming current work directory contains RISCVSimulator package directory:
```
java --module-path %PATH_TO_FX% --add-modules javafx.fxml,javafx.base,javafx.controls,javafx.graphics RISCVSimulator.Main
```

