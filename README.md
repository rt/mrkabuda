# mrkabuda

Configurable way to make a 3D keyboard model.

http://adereth.github.io/blog/2014/04/09/3d-printing-with-clojure/

**Generating the design**
* Run `lein repl`
* Load the file `(load-file "src/mrkabuda/core.clj")`
* This will regenerate the `things/*.scad` files
* Use OpenSCAD to open a `.scad` file.
* Make changes to design, repeat `load-file`, OpenSCAD will watch for changes and rerender.
* When done, use OpenSCAD to export STL files

### Cases

#### Drawing a smooth surface

I'm pretty stuck this seems hard.

https://stackoverflow.com/questions/7008006/draw-good-looking-bezier-curve-through-random-points

- Cubic splines
- Piecewise polynomial


http://forum.openscad.org/Spline-interpolation-nSpline-td15207.html

#### Make with shapes

The parameters are already known so we can adjust a shaped base to fit.
Imagine a curved edge (cylinder hulled top) with a inner sphere, angle, pronate

- tenting angle
- pronation angle
- curvature
- etc.

**Basic fixed size case**
- Tent/pronate large surface
- Union case and se

Cut out points need to be mapped to case (case-cut-out-points)
Hull/connect plate to case-cut-out-points

### Plates mounting

Having plate options allows for more customization

- Integrated with the case
- Bottom mount to support a plate
- Top mount needs a special upper piece to screw the plate into (this seems clean as feel comes directly from the plate only)
- Tray mount lets plate sit on pcb (this would get support from posts and the pcb)

Seems integrated and top mount would be good options.

https://thomasbaart.nl/2019/04/07/cheat-sheet-custom-keyboard-mounting-styles/
https://keyboard.university/200-courses/keyboard-mounting-styles

### Parts

- provide a model/shape: these would let you configure the model. 
- give dimensions/specs: there probably is a good way to organize this such that the implementor can use both the model and spec.

```clojure
(def part {
  :type-a {
    :spec {}
    :model {}
  }
  :type-b {
    :spec {}
    :model {}
  }
})
```

### Plates

oxidation-proof coating with a sandblasted finish

**Foam**

what material?


### Problems

```clojure
;; offset doesn't see to work
(spit "things/fun.scad" (write-scad (offset {:r 5} (square 40 50))))
```
