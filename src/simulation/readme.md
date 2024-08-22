# Simulation Package README

## Overview

This package contains files that implement simulation within aldb. 

## Contents

### 1. `Constaint/Alias Manager.java`
- **Purpose:** 
  - Contains functions for managing and validating constraints/aliases used by the break command.
  
### 2. `GraphPrinter.java`
- **Purpose:** 
  - Runs graphviz commands in console to generate .png files or .json files for visualization.
  
### 3. `SimulationManager.java` 
- **Purpose:** 
  - Contains functions for simulation activities. Stores statePath and stateGraph that represent an alloy trace. Interacts with the alloy solver to take steps/alt/reverse, etc.
  
- 
