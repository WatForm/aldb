# State Package README

## Overview

This package contains StateGraph, StatePath, StateNode objects that together stores and describes the states within a trace.

## Contents

### 1. `StateNode.java`
- **Purpose:** 
  - This class defines the structure of a StateNode. A StateNode stores its state information in a map and the StateNodes it can reach within one outgoing edge in a list of StateNodes
  
### 2. `StatePath.java`
- **Purpose:** 
  - This class defines the structure of a StatePath. A StatePath is a list of StateNodes in the current path taken.
  

### 3. `StateGraph.java`
- **Purpose:** 
  - This class defines the structure of a StateGraph. A StateGraph is a list of all StateNodes visited.
  