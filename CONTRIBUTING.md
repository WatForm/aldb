# Contributing

Prior to submitting a PR, please ensure that:

1. The proposed change has been clearly documented and motivated in a GitHub issue and discussed with the owners of this repository.
2. There is no existing or obsolete ticket which addresses your proposed change.

## Build Requirements

* Java 8 or later
* [Ant](https://ant.apache.org/) 1.10.7 or later
* Note: org.alloytools.alloy.dist-6.0.0.jar is included in lib (from https://github.com/AlloyTools/org.alloytools.alloy/releases)

## Build and Run

     $ ant
     $ java -jar dist/aldb.jar

## Running Tests

     $ ant test
     
## Pull Request Process

1. Ensure your code builds successfully.
2. Test your changes manually if possible by running the JAR and manipulating a non-trivial model if applicable.
3. Ensure that existing tests pass.
4. Ensure that you have added new tests where necessary.
5. Write a descriptive and concise commit message, PR title, and description.
6. Ensure that a GitHub issue is referenced in the PR description with a `Closes` statement.
7. Create your PR.
8. Once all status checks pass, assign one or more owners as reviewers.
9. Once you have at least one approval from an owner, you can squash and merge your PR.
