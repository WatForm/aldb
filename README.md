# ALDB

![CI](https://github.com/WatForm/aldb/workflows/CI/badge.svg)

Alloy Debugger (ALDB) is a command-line debugger for transition systems modelled in Alloy. Transition systems are composed of some state that changes based on a transition relation.

ALDB allows for incremental state-space exploration of Alloy transition systems, with the aim of reducing the time taken to validate and/or find bugs in a model. It can also incrementally explore XML counterexamples that are generated by the Alloy Analyzer.

This guide explains usage of ALDB, compatibility requirements for Alloy models, and troubleshooting. It also includes an illustrative example of stepping through a concrete transition system using ALDB.

## Table of Contents

* [Getting Started](#getting-started)
* [Model and Configuration Format](#model-and-configuration-format)
* [Commands](#commands)
* [Usage Example](#usage-example)
* [Trace Mode](#trace-mode)
* [Session Recovery](#session-recovery)
* [Troubleshooting](#troubleshooting)
* [Reporting Issues](#reporting-issues)
* [Contributing](#contributing)
* [Contact](#contact)

## Getting Started

1. Download the latest JAR from the [releases](https://github.com/WatForm/aldb/releases) or clone this repo and build ALDB following the instructions in the [contributing guildlines](./CONTRIBUTING.md). Note that the master branch points to the latest, unstable, development version of ALDB.

2. Run ALDB from the command line:
    ```sh
    $ java -jar <path-to-aldb.jar>
    ```
    
3. Instructions on building ALDB can be found in CONTRIBUTING.md

## Model and Configuration Format

ALDB supports transition systems modelled in a certain style in Alloy. As such, there are certain signatures and predicates that are expected to exist, whose names can be stored in a custom configuration.

The configuration must be defined in YAML. It can be specified within a comment block in the model file (to be applied for that model only), or set via passing a separate YAML file to the `set conf` command.
When using the `set conf` command, the configuration will last for the entire ALDB session.

The default configuration looks like the following:

```yml
stateSigName: State
transitionRelationName: next
initPredicateName: init
additionalSigScopes: {}
```

`stateSigName` is the name of the signature that represents the changing state that the transition system tracks.

`transitionRelationName` is the name of the transition relation that changes the state.

`initPredicateName` is the name of the predicate that defines the initial state of the transition system.

`additionalSigScopes` is a map of (String, Integer) pairs that define specific scopes for other signatures within the model.

The Alloy code that conforms to the above configuration – with the configuration given in a comment block with the required header and footer – looks like the following:

```Alloy
/*  BEGIN_ALDB_CONF
 *
 *  stateSigName: State
 *  transitionConstraintName: next
 *  initPredicateName: init
 *
 *  END_ALDB_CONF
 */

sig State { … }
pred init[s: State] { … }
pred next[s, s’: State] { … }
```

Refer to the worked example in this guide for a sample of a concrete Alloy model that is supported by ALDB.

## Commands

### Summary

Command | Description
-- | --
[alias](#alias) | Control the set of aliases used
[alt](#alt) | Select an alternate execution path
[break](#break) | Control the set of constraints used
[current](#current) | Display the current state
[dot](#dot) | Dump DOT graph to disk
[help](#help) | Display the list of available commands
[history](#history) | Display past states
[init](#init) | Return to the initial state of the active model
[load](#load) | Load an Alloy model
[quit](#quit) | Exit ALDB
[reverse-step](#reverse-step) | Go back n steps in the current state traversal path
[scope](#scope) | Display scope set
[set](#set) | Set ALDB options
[step](#step) | Perform a state transition of n steps
[trace](#trace) | Load a saved Alloy XML instance
[until](#until) | Run until constraints are met

### Detailed Descriptions

#### alias
The `alias [-c] [-l] [-rm alias] [alias formula]` command allows for assigning a shorthand alias for a formula. These aliases can be used when adding constraints via the `break` command or specifying constraints in the `step` command.

Specify the `-c` option to clear all aliases.
Specify the `-l` option to list all current aliases.
Specify the `-rm alias` option to remove an alias.

An existing alias can be used in the definition of a new alias. To do so, wrap the existing alias in backticks when specifying the formula for the new alias.

```
(aldb) alias a 1
(aldb) alias b `a`+1
(aldb) alias -l

Alias           Formula
a               1
b               1+1

```

#### alt
The `alt [-r]` command switches between alternative execution paths in the current trace. Multiple unique states can be reached for a given path length; this command allows the user to explore all those states.

Specify the `-r` option to go back to the previous execution state.

![image](https://user-images.githubusercontent.com/13455356/79278612-08626f00-7e7a-11ea-98b8-f8e59e8d12f5.png)

#### break
The `break [-c] [-l] [-rm num] [constraint]` command allows for specifying an execution constraint (breakpoint). Use these constraints to control certain parameters and invariants throughout the model’s execution. When stepping through the model, execution will halt if and when the constraint is satisfied. Running the command multiple times will result in a super-constraint composed of a disjunction of each individually-specified constraint.

Specify the `-c` option to clear all constraints.
Specify the `-l` option to list all current constraints.
Specify the `-rm num` option to remove a constraint.

#### current
The `current [property]` command displays the values of all properties at the present execution state.

Specify a specific property to view only its values.

#### dot
The `dot` command writes the DOT graph representation of the states visited in the active model to a new file in the current working directory.

#### help
The `help [cmd]` command lists available commands to use in ALDB, along with descriptions of what they do.

Specify a cmd to view full documentation for that specific command.

#### history
The `history [n]` command displays the past n consecutive states of the current execution path.

Specify an integer value of n >= 1. By default, n = 3.

![image](https://user-images.githubusercontent.com/13455356/77835857-59a6fa80-7127-11ea-8ba4-79563a279c47.png)

#### init
The `init` command returns the user to the initial state of the active model.

#### load
The `load <filename>` command loads and initializes the Alloy model specified by <filename> into ALDB. It will check for a {BEGIN | END}_ALDB_CONF comment block in <filename> to set the configuration, and then initialize the state graph to the initial state specified by the equivalent `init` predicate in the model.

#### quit
The `quit` command exits ALDB.

#### reverse-step
The `reverse-step [n]` command goes back by n steps in the current execution path. If n is larger than the current path length, this command takes the execution back to the initial state.

Specify an integer value of n >= 1. By default, n = 1.

![image](https://user-images.githubusercontent.com/13455356/77835867-793e2300-7127-11ea-9b10-bd670fe75a2b.png)
#### scope
The `scope [sig-name]` command displays the scope set for all signatures in the active model.

Specify a sig-name to view the scope only for that specific signature.

#### set
The `set <option> <value>` command allows users to modify ALDB options.

##### Available options:

1) `set conf [filename]`

    This command sets the custom parsing configuration for the current session. For more information, see [Model and Configuration Format](#model-and-configuration-format).

2) `set diff <on | off>`

    This command turns on/off differential output for [`step`](#step) and [`alt`](#alt). When enabled, only fields that have changed between the previous and current state are displayed. By default, this option is enabled.

#### step
The `step [n | constraints]` command performs n state transitions from the current execution state, ending at one of the valid states for a length (current + n) state traversal from the initial state.

Specify an integer value of n >= 1. By default, n = 1.

Alternatively, step constraints can be specified, as a comma-separated list enclosed by square brackets.
n is equal to the number of items in the list.
The i-th constraint is applied when performing the i-th transition.

![image](https://user-images.githubusercontent.com/13455356/79278678-347df000-7e7a-11ea-90d7-111733a448a6.png)

#### trace
The `trace <filename>` command loads a saved Alloy XML trace generated by the Alloy Analyzer.

Specify the file to load from as `filename`. The contents must be in the Alloy XML format.

#### until
The `until [limit]` command will run several forward steps of the transition system until it meets user-specified constraints. See the `break` command documentation for information about how to specify multiple constraints.

Specify the `limit` in order to constrain the search space. In other words, ALDB will only check up to `limit` state transitions, and if the constraints have not been satisfied by then, it will not check any further transitions. `limit` must be an integer >= 1. By default, limit = 10.

![image](https://user-images.githubusercontent.com/13455356/77835884-9a067880-7127-11ea-8808-16b692ee3ebe.png)

## Usage Example

This section will walk through solving the classic River Crossing Problem (RCP) via specification with Alloy and ALDB.

In the RCP, there is a river dividing two sides of land. There is one boat, and it can carry a human and one other item or animal. A farmer, his fox, chicken, and a bag of grain are all on the near side of the river. The farmer must transport everything to the far side. There are some stipulations:

- If the fox and chicken are on the same side without the farmer, then the fox eats the chicken.
- If the chicken and grain are on the same side without the farmer, then the chicken eats the grain.

Solving the RCP entails finding a sequence of events that allows everything to be safely transported to the far side of the river. The following is an Alloy model representing the problem:

```Alloy
/* Farmer and his possessions are objects. */
abstract sig Object { eats: set Object }
one sig Farmer, Fox, Chicken, Grain extends Object {}

/* Defines what eats what when the farmer is not around. */
fact { eats = Fox->Chicken + Chicken->Grain }

/* Stores the objects at the near and far sides of the river. */
sig State { near, far: set Object }

/* In the initial state, all objects are on the near side. */
pred init [s: State] {
  s.near = Object && no s.far
}

/* At most one item to move from ‘from’ to ‘to’. */
pred crossRiver [from, from’, to, to’: set Object] {
  one x: from | {
    from’ = from - x - Farmer - from’.eats
    to’ = to + x + Farmer
  }
}

/* Transition to the next state. */
pred next [s, s’: State] {
  Farmer in s.near =>
    crossRiver [s.near, s’.near, s.far, s’.far]
  else
    crossRiver [s.far, s’.far, s.near, s’.near]
}
```
[http://alloytools.org/tutorials/online/frame-RC-1.html](http://alloytools.org/tutorials/online/frame-RC-1.html)

The changing state in the above model is the set of objects that are on the near and far sides. Initially, nothing is on the far side. The river can only be crossed starting from where the farmer currently is, and ending on the opposite side. When crossing the river, the farmer has the option to take any one of the other objects with them, with the consequence of something on that side potentially being eaten when left unattended.

The goal is to use ALDB to find a set of state transitions that allows all objects to eventually safely reach the far side of the river.

Begin by loading the model into ALDB. There is no need for a configuration here because the model as written conforms to the default names that ALDB uses for states and transition functions: State, init, next.

```
(aldb) load models/river_crossing.als
Reading model from models/river_crossing.als...done.
(aldb) current

S1
----
far: {  }
near: { Chicken, Farmer, Fox, Grain }
```

The initial state is set as expected. Since the desired end state is known, using the until command with a breakpoint is the quickest method to get there. The breakpoint will be set by constraining the fair side to contain all the objects, and having nothing on the near side, as follows:

```
(aldb) break "far = Object && no near"
(aldb) until

S8
----
far: { Chicken, Farmer, Fox, Grain }
near: {  }
```

Observe that the desired state has been reached. Use the history command to show the sequence of state transitions that got the execution to this point.

```
(aldb) history 10

S1 (-7)
---------
far: {  }
near: { Chicken, Farmer, Fox, Grain }

S2 (-6)
---------
far: { Chicken, Farmer }
near: { Fox, Grain }

S3 (-5)
---------
far: { Chicken }
near: { Farmer, Fox, Grain }

S4 (-4)
---------
far: { Chicken, Farmer, Fox }
near: { Grain }

S5 (-3)
---------
far: { Fox }
near: { Chicken, Farmer, Grain }

S6 (-2)
---------
far: { Farmer, Fox, Grain }
near: { Chicken }

S7 (-1)
---------
far: { Fox, Grain }
near: { Chicken, Farmer }
```

Now it is known that after seven transitions corresponding to the above, the desired end state can be reached.

Alternatively, ALDB can incrementally explore the model’s state space by stepping.

```
(aldb) load models/river_crossing.als
Reading model from models/river_crossing.als...done.
(aldb) current

S1
----
far: {  }
near: { Chicken, Farmer, Fox, Grain }

(aldb) step

S2
----
far: { Farmer, Grain }
near: { Fox }
```

Observe that the reached state does not contain all the objects. The chicken is missing because in this specific execution path, the farmer took the grain and left the fox to eat the chicken. This behaviour is undesired, so the alt command can be used to explore other states that could have been reached with the same path length.

```
(aldb) step

S2
----
far: { Farmer, Grain }
near: { Fox }

(aldb) alt

S3
----
far: { Farmer, Fox }
near: { Chicken }

(aldb) alt

S4
----
far: { Chicken, Farmer }
near: { Fox, Grain }
```

In the first alternate state, the farmer took the fox, leaving the chicken to eat the grain. The second alternate state is the desired one. This process of manually exploring the state space exposes execution states that may not otherwise have been explored. Continue this process to eventually reach the desired end state, or to simply explore other intermediate states.

## Trace Mode

The Alloy Analyzer is able to generate counterexamples when inconsistencies are found during model checking. The counterexample is specified as an XML file, and then loaded into ALDB for exploration.

Use the `trace <filename>` command to load the counterexample into ALDB. Now it is possible to incrementally explore the counterexample states using the usual ALDB functions.

Note that the original Alloy model from which the counterexample was generated need not be provided. If it is not, ALDB is unable to find alternate states or step beyond the final state of the counterexample.

## Session Recovery

Every unique execution of ALDB is considered to be a session. Each time ALDB is started, a session log is created under the directory referred to by `$TMPDIR`, with a naming scheme of: `aldb.<yyyy>.<MM>.<dd>.<HH>.<mm>.<ss>`. This file records each full, completed command entered in the current session.

If a session is terminated at any point, it can be recovered up to the point of the last completed command by starting ALDB with the `--restore` flag (`-r` shorthand) and the file path of the desired session log to restore from. A new session log with the contents of the previous session’s log (and any further commands) will be created for the new session.

## Troubleshooting

Error message | Solution
-- | --
No such file. | Ensure that the file exists, and that the file path being provided is correct.
No file specified. | If the command requires a file, specify the file path after the command name.
Failed to read file. | The file exists but could not be read. Restart ALDB and try again.
Invalid configuration. | Ensure that the configuration is correctly specified in syntactically-valid YAML.
Undefined command. | The command does not exist. Ensure no typos.
Signature not found. | Ensure that the Sig being requested by the `scope` command exists in the model that was loaded.
No model file specified. | Use the `load` command to load an Alloy model, and then retry the action.
Session log could not be opened for reading. | Ensure that the given session log path is correct and the file exists.
Unable to create session log. | Restart ALDB and try again. If this error continues to occur, restart the computer.
Predicate not found. | Ensure that the predicate name specified in the configuration exists in the Alloy model.
Issue parsing predicate. | Ensure that the Alloy model is syntactically-valid.
I/O failed. | Restart ALDB and try again. If this error continues to occur, restart the computer.
Internal error. | Ensure that you are using the latest version of ALDB. If the error continues to occur, please [report an issue](#reporting-issues).

## Reporting Issues

If you discover an issue, please report it by creating a GitHub issue. In the issue, please be descriptive and include as much of the following information as possible:

1. ALDB version
2. Java version
3. OS version
4. Reproduction steps
5. Screenshots
6. Any other useful information

## Contributing

Please see [the contributing guidelines](CONTRIBUTING.md).

## Contact

For further discussion, questions, or anything else that may be unclear, please email nday@uwaterloo.ca.

