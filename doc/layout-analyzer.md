# layout-analyzer


I'm trying to build a more accurate keyboard analyzer.


* Layouts: The user will be able to keep multiple layouts to compare/analyze.
 These also might represent the keyboards that the user had.

* AnayzerConfigurations: The user will be able to keep multiple configurations to apply.

* Results: These are the results produced when appling a conguration to a layout.

### key-config

Key configs are shared for layout-editor, layout-analyzes, and the physical build.

**layout-editor propeties**

```clojure
```

Layer
```javascript
        this.mod = this.mod || null;
        this.modPenalty = this.modPenalty || 0;
        this.rows = this.rows || [];
```

```javascript
    /**
     * Generate effort maps for each key
     */
    generateMaps(config) {

    }

    /**
     * Generate the default map when prevKey, etc not concerned
     */
    generateDefaultMap(config) {
    }

    run(text) {
        let prevKey = null;
        let lastOppositeHandKey = null; //last hand is not necessarally last char

        //for chars
        //
        //find currentKey
        //
        //prevKey.getEffort(currentKey, defaultMap);
    }
```

### Physical Keyboard

A physical keyboard has key positions determined by the width of the keys.  
We won't argu about the column staggar as the length of the users finger will vary.


## Anaysis

* Finger distance from home row:
* Finger penalties (and by row)


#### Same Hand Weights

This could handle/include opposite hand in the sense that opposite hand would not incur same hand penalties.

* Same finger penalty
* Same hand outward penalty
* Same hand inward optimization
* Opposite hand optimization
* Prev key distance (and considered last use).  Like, if past 3 chars then consider finger has returned to home position.
* Same hand modifier (usually would use osl but if multiple keys pressed in a layout, should be analyzed differently.

#### Matrix for each key

You could also have a effort matrix for each key (rather that rules)
Pre-generate these from the rules. Should be faster too because it'll be a lookup.





## Build a layout (Phase 2)

* Fixed keys: Some keys you might not want to move
* Any number of keys: Define what you want on the first layer
* Block keys: Say you don't want to use certain positions, or reserve for modefiers.

* Auto optimization

This is too big (26!), you would probably need to fix many first.
Or, give each key limited options.


* 26 + , . ; ' /
*


- Map
- Fix
- Allocate keys
- Run


OptimizerKey
- x,y
- isBlocked
- char
- next optimizer key (tell what is taken for the current run)


run (taken) {
  for taken
    
    next.run(taken - current char)



Should do this with just the char and display for 4 chars to show that they were all processed 

