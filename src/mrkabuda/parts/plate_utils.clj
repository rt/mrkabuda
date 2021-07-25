(ns mrkabuda.parts.plate_utils
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [clojure.pprint :refer [pprint]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))

;; (def min-point [Integer/MIN_VALUE Integer/MIN_VALUE Integer/MIN_VALUE])
;; (def max-point [Integer/MAX_VALUE Integer/MAX_VALUE Integer/MAX_VALUE])

(defn- flatten-points 
  "Get all points of the plate usually to find max/min, etc."
  [points]
  (for [row points, pair row, point pair]
    point))

(defn min-point [points]
  (apply mapv min (flatten-points points)))

(defn max-point [points]
  (apply mapv max (flatten-points points)))

(defn min-x [points]
  (nth (min-point points) 0))

(defn min-y [points]
  (nth (min-point points) 1))

(defn min-z [points]
  (nth (min-point points) 2))

(defn max-x [points]
  (nth (max-point points) 0))

(defn max-y [points]
  (nth (max-point points) 1))

(defn max-z [points]
  (nth (max-point points) 2))

(defn center-point 
  "If min and max have the same polarity"
  [points]
  (map #(/ % 2) (map - (max-point points) (min-point points))))

(defn center-point-opposite-polarity 
  "If min and max have opposite polarity"
  [points]
  (map #(/ % 2) (map + (max-point points) (min-point points))))

(defn center-points [points]
 (map - points (center-point points)))

(defn plate-width [points]
  (- (max-x points) (min-x points)))

(defn plate-height [points]
  (- (max-y points) (min-y points)))

(defn boundary-points [side points offset]
  (case side
    :top (mapv (fn [pair] (mapv #(mapv + [0 offset 0] %) pair)) (first points))
    :bottom (mapv (fn [pair] (mapv #(mapv + [0 (- offset) 0] %) pair)) (last points))
    :left (mapv (fn [pair] (mapv #(mapv + [(- offset) 0 0] %) pair)) (map first points))
    :right (mapv (fn [pair] (mapv #(mapv + [offset 0 0] %) pair)) (map last points)))
)

(defn direction-vector
  "Get direction-vector"
  [p1 p2]
  (map (fn [ele] (if (not= ele 0.0) ele (/ 1 Integer/MAX_VALUE))) (map - p2 p1)))

(defn scalar-val 
  "p2 = p1 + dir-vect*scalar" 
  [p1 p2]
  (/ (- (first p2) (first p1)) (first (direction-vector p1 p2))))

(defn scalar-from-pair [pair]
  (apply scalar-val pair))

(defn plate-scalar [points]
  (scalar-from-pair (get-in points [0 0])))

(defn plate-botton-point [plate-scalar dir-vect top-point]
  (map + top-point (map * (repeat 3 plate-scalar) dir-vect)))

(defn point-on-line-given-x-and-direction-vector
  "Get pair against two pair points (that make a line (pair line)) when x"
  ;; http://mathcentral.uregina.ca/QQ/database/QQ.09.01/murray2.html
  [p1 dir-vect x]
  (let [
        param (/ (- x (nth p1 0)) (nth dir-vect 0))
        y (+ (* param (nth dir-vect 1)) (nth p1 1))
        z (+ (* param (nth dir-vect 2)) (nth p1 2))
        ]
    (vector x y z)))

(defn point-on-line-given-x
  "Get pair against two pair points (that make a line (pair line)) when x"
  ;; http://mathcentral.uregina.ca/QQ/database/QQ.09.01/murray2.html
  [p1 p2 x]
  (let [ dir-vect (direction-vector p1 p2) ]
    (point-on-line-given-x-and-direction-vector p1 dir-vect x)))

(defn create-pair-at-x
  "Pair1 and 2 are the endpoints of the segment in which the new pair at x will be created"
  [pair1 pair2 x]
  (let [
        [p1-top p1-bot] pair1
        [p2-top p2-bot] pair2
        dir-vect (direction-vector p1-top p2-top)
        dir-vect-plate (direction-vector p1-top p1-bot)
        top-point (point-on-line-given-x-and-direction-vector p1-top dir-vect x)
        bot-point (plate-botton-point (scalar-from-pair pair1) dir-vect-plate top-point)
        ]
  (vector top-point bot-point)))

(defn map-boundary-points 
  "Map boundaray points to a z plane/panel"
  [side points offset z]
  (mapv #(assoc % 2 z) (mapv first (boundary-points side points offset))))

(defn plate-pairs
  "Make top/bot pair points given the middle point"
  [points thickness]
  (mapv #(vector
          (map + [0 0 thickness] %)
          %
          ) points)
  )

(defn boundary-corner-points [corner points z offset thickness]
  "In clockwise order"
  (case corner
    :tl (plate-pairs (vector 
                       (first (map-boundary-points :left points offset z))
                       ;; top-left
                       (first (map-boundary-points :top points offset z))
                       ) thickness)
    :tr (plate-pairs (vector 
                       (last (map-boundary-points :top points offset z))
                       ;; top-right
                       (first (map-boundary-points :right points offset z))
                       ) thickness)
    :br (plate-pairs (vector 
                       (last (map-boundary-points :right points offset z))
                       ;; bottom-right
                       (last (map-boundary-points :bottom points offset z))
                       ) thickness)
    :bl (plate-pairs (vector 
                       (first (map-boundary-points :bottom points offset z))
                       ;; bottom-left
                       (last (map-boundary-points :left points offset z))
                       ) thickness)
    ))

(defn boundary-points-all [points offset]
  "Get all boundary points, taken clockwise so that polygon can be created"
  (concat 
    (boundary-points :top points offset)
    (boundary-points :right points offset)
    (reverse (boundary-points :bottom points offset))
    (reverse (boundary-points :left points offset))))

(defn switch-points
  "Get points as array of switch points (four points per switch)"
  [points]
  (vec
    (for [row (range 0 (count points) 2)
          :let [
                ;; top/bottom have the same amount of points
                switch-top (map vec (partition 2 (nth points row)))
                switch-bot (map vec (partition 2 (nth points (inc row))))
                ]
          ]
      (vec
        (for [ col (range 0 (count switch-top)) ]
          (vector (nth switch-top col) (nth switch-bot col))
          ))
      )))

(defn switch-point
  "Return post (pair) in switch"
  [switch pos]
  (case pos
    :tl (get-in switch [0 0])
    :tr (get-in switch [0 1])
    :bl (get-in switch [1 0])
    :br (get-in switch [1 1])
    ))

(defn switch-point-offset
  "Apply offset to post (pair)"
  [switch pos offset-pos offset]
  (let [ post (switch-point switch pos) ]
    (case offset-pos
      :left (map #(map - % [offset 0 0]) post)
      :right (map #(map + % [offset 0 0]) post)
      :bottom (map #(map - % [0 offset 0]) post)
      :top (map #(map + % [0 offset 0]) post)
      )))

(defn switch-row-col
  [switches current-switch]
  (for [
        row (range 0 (count switches))
        column (range 0 (count (nth switches row)))
        :let [ switch (get-in switches [row column]) ]
        :when (= (first (switch-point current-switch :tl)) (first (switch-point switch :tl))) 
        ]
    [row column]
    ))

(defn same-switch 
  "no two switches will have any same points"
  [s1 s2]
  (= (first (first (switch-point s1 :tl))) (first (first (switch-point s2 :tl)))))

(defn- find-next
  [look-down? current-switch current-pos current-offset-pos offset switches]
  (let [
        compare-coord { :top first :right second :bottom first :left second }
        compare-coord-opposite { :top second :right first :bottom second :left first }
        ]
    (reduce (fn [acc row]
              (reduce (fn [acc switch] 
                        (let [
                              current-switch-compare-pt (first (switch-point-offset current-switch current-pos current-offset-pos offset))
                              compare-pts-next (fn [look-down?]
                                                 (if look-down?
                                                   {
                                                    :top [[:tl :top] [:tr :right]]
                                                    :right [[:tr :right] [:br :bottom]]
                                                    :bottom [[:br :bottom] [:bl :left]]
                                                    :left [[:bl :left] [:tl :top]]
                                                    }
                                                   {
                                                    :top [[:tl :left] [:bl :left]]
                                                    :right [[:tl :top] [:tr :top]]
                                                    :bottom [[:tr :right] [:br :right]]
                                                    :left [[:bl :bottom] [:br :bottom]]
                                                    }
                                                   )
                                                 ) 
                              positive-direction? (fn [current-offset-pos] (or (= current-offset-pos :top) (= current-offset-pos :right))) 
                              positive-direction-blah? (fn [current-offset-pos] (or (= current-offset-pos :top) (= current-offset-pos :left))) 
                              compare-positions (current-offset-pos (compare-pts-next look-down?))
                              distance (fn [pt1 pt2]
                                         (Math/sqrt (+ (Math/pow (- (first pt2) (first pt1)) 2) (Math/pow (- (second pt2) (second pt1)) 2))))
                              ]
                          (reduce (fn [acc [pos offset-pos]]
                                    (let [
                                          switch-compare-pt (first (switch-point-offset switch pos offset-pos offset))
                                          ret {:switch switch :pos pos :offset-pos offset-pos}
                                          ]
                                      ;; must have in general
                                      (if
                                        (and
                                          ;; perpendicular axis to direction
                                          (let [
                                                perpendicular-condition? ((if look-down?
                                                                            (if (positive-direction? current-offset-pos) <= >=)
                                                                            (if (positive-direction? current-offset-pos) > <)
                                                                            )
                                                                          ((current-offset-pos compare-coord-opposite) switch-compare-pt)
                                                                          ((current-offset-pos compare-coord-opposite) current-switch-compare-pt))
                                                _ (prn "perpendicular-condition: " perpendicular-condition? current-pos current-offset-pos (positive-direction? current-offset-pos) (switch-row-col switches switch))
                                                ]
                                            perpendicular-condition?)

                                          ;; horizontal axis to direction
                                          (let [
                                                horizontal-condition? ((if (positive-direction-blah? current-offset-pos) > <)
                                                                       ((current-offset-pos compare-coord) switch-compare-pt)
                                                                       ((current-offset-pos compare-coord) current-switch-compare-pt))
                                                _ (prn "horizontal-condition: " horizontal-condition?)
                                                ] 
                                            horizontal-condition?)

                                          ;; must be close (next col)
                                          (let [
                                                close-enough? 
                                                ;; (and 
                                                ;;                 ((if (positive-direction-blah? current-offset-pos) > <)
                                                ;;                  ((current-offset-pos compare-coord) switch-compare-pt)
                                                ;;                  ((current-offset-pos compare-coord) current-switch-compare-pt))
                                                ;;                 ((if (positive-direction-blah? current-offset-pos) < >)
                                                ;;                  ((current-offset-pos compare-coord) switch-compare-pt)
                                                ;;                  ((if (positive-direction-blah? current-offset-pos) + -) ((current-offset-pos compare-coord) current-switch-compare-pt) 19)
                                                ;;                  )
                                                ;;                 )
                                                (<
                                                 (distance switch-compare-pt current-switch-compare-pt)
                                                 25
                                                 )
                                                _ (prn "close-enough: " close-enough?)
                                                ] 
                                            close-enough?)

                                          ;; better than acc
                                          (let [
                                                better-than-current? (or 
                                                                       (nil? acc)
                                                                       (< 
                                                                         (distance switch-compare-pt current-switch-compare-pt) 
                                                                         (distance 
                                                                           (first (switch-point-offset (:switch acc) (:pos acc) (:offset-pos acc) offset)) 
                                                                           current-switch-compare-pt)
                                                                         )
                                                                       ;; only need to compare closeness to current?
                                                                       ;;   ((if (or (= current-offset-pos :top) (= current-offset-pos :left)) >= <=)
                                                                       ;;    ((current-offset-pos compare-coord-opposite) switch-compare-pt)
                                                                       ;;    ((current-offset-pos compare-coord-opposite) switch-best-compare-pt))
                                                                       )
                                                _ (prn "better-than-current: " better-than-current?)
                                                ]
                                            better-than-current?)
                                          )
                                        ret
                                        ;; keep current best
                                        acc
                                        )
                                      )
                                    ) acc compare-positions)
                          ))
                      acc row))
            nil switches))
)
  

(defn process-boundary
  ;; "boundary-fn is passed the original pts, the boundary pts (row/col?)"
  [iteration boundary-fn params offset switches start-switch]
  (let [
        current-switch (:switch params)
        current-pos (:pos params)
        offset-pos (:offset-pos params)
        clockwise-positions {
                             :tl {
                                  :next-pt :tr
                                  :tail-offset-pos :top
                                  :head-offset-pos :left
                                  }
                             :tr {
                                  :next-pt :br
                                  :tail-offset-pos :right
                                  :head-offset-pos :top
                                  }
                             :br {
                                  :next-pt :bl
                                  :tail-offset-pos :bottom
                                  :head-offset-pos :right
                                  }
                             :bl {
                                  :next-pt :tl
                                  :tail-offset-pos :left
                                  :head-offset-pos :bottom
                                  }
                             }
        tail? (fn [pos offset-pos]
                ;; (some #{(vector pos offset-pos)} (vector [:tl :top] [:tr :right] [:br :bottom] [:bl :left]))
                (= (:tail-offset-pos (pos clockwise-positions)) offset-pos)
                )
        next-clockwise-offset-pos (fn [pos offset-pos]
                                    (let [ ]
                                      (if (tail? pos offset-pos) 
                                        (:head-offset-pos ((:next-pt (pos clockwise-positions)) clockwise-positions))
                                        (:tail-offset-pos (pos clockwise-positions))
                                        ))
                                    )
        head-fn (fn [current-switch]
                  (if (tail? current-pos offset-pos)
                  (let [
                                next-params {
                                             :switch current-switch
                                             :pos (:next-pt (current-pos clockwise-positions))
                                             :offset-pos (next-clockwise-offset-pos current-pos offset-pos)
                                             }
                                ]
                            (prn "tail")
                            (pprint next-params)
                            (boundary-fn params next-params)
                            next-params
                            )
                  (let [ 
                        _ (prn "----- looking up -----") 
                        nearest-out (find-next false current-switch current-pos offset-pos offset switches) 
                        ]
                    (if (nil? nearest-out) 
                      (let [ 
                        _ (prn "----- looking down -----") 
                            nearest-in (find-next true current-switch current-pos offset-pos offset switches) 
                            ]
                        (if (nil? nearest-in)
                          ;; found nothing, next point on switch
                          (let [
                                next-params {
                                             :switch current-switch 
                                             :pos current-pos
                                             :offset-pos (next-clockwise-offset-pos current-pos offset-pos)
                                             }
                                ]
                            (prn "nothing")
                            (pprint next-params)
                            (boundary-fn params next-params)
                            next-params
                            )
                          ;; attach to next-tl-top (but depending on the distance might want to corner down first)
                          (let []
                            (prn "nearest-in")
                            (pprint nearest-in)
                            (boundary-fn params nearest-in)
                            nearest-in
                            )
                          ))

                      ;; attach to next-tl-left (actually, should do check distance and maybe attach to bl or to the pependicular)
                      (let [] 
                        (prn "nearest-out")
                        (pprint nearest-out)
                        (boundary-fn params nearest-out)
                        nearest-out
                        )
                      )
                    )))
        ]
    (let [
          next-params (head-fn current-switch)
          _ (prn "next switch: " (switch-row-col switches (:switch next-params)))
          ]
      (if-not (and 
                (same-switch start-switch (:switch next-params))
                (= (:pos next-params) :tl)
                (= (:offset-pos next-params) :top)
                ) 
        (if (not= iteration 200)
        (recur (inc iteration) boundary-fn next-params offset switches start-switch)
        )
        ))
    )
  )

(defn boundary-points-all-new
  "Return boundary-points
  - Move right looking up, then down until hit right-most switch
  - Change direction left looking down, then up until hit start switch"
  [points offset]
  (let [
        boundary-points (atom [])
        switches (switch-points points)
        start-switch (get-in switches [0 0])
        boundary-fn (fn [params next-params] 
                      (swap! boundary-points conj (switch-point-offset (:switch next-params) (:pos next-params) (:offset-pos next-params) offset))
                      )
        ]
    (process-boundary 0 boundary-fn {:switch start-switch :pos :tl :offset-pos :top} offset switches start-switch)
    ;; (pprint @boundary-points)
    @boundary-points
    )
  )

(defn upper-boundary-points [points offset]
  "Get list of 2 dimensional boundary points"
  (mapv #(vector (nth % 0) (nth % 1)) 
       (map first (boundary-points-all points offset))
       ))

(defn map-boundary-to-level
  [points side z offset thickness]
  (vector
        (plate-pairs (map-boundary-points side points offset z) thickness)
        (boundary-points side points 0)
        ))

(defn mirror-plate [row]
  (reverse
    (for [pair row]
      (vector
        (map * [-1 1 1] (first pair))
        (map * [-1 1 1] (second pair))))))

(defn tilt-plate-row [angle row]
  (for [pair row]
    (vector
      (rotate-around-x angle (first pair))
      (rotate-around-x angle (second pair))
      )))

(defn tilt-plate [angle points]
  (map (partial tilt-plate-row angle) points))

(defn pronate-plate [angle row]
  (for [pair row]
    (vector
      (rotate-around-z angle (first pair))
      (rotate-around-z angle (second pair))
      )))

(defn tent-plate [angle row]
  (for [pair row]
    (vector
      (rotate-around-y angle (first pair))
      (rotate-around-y angle (second pair))
      )))

(defn tent-and-pronate-plate [tenting-angle pronation-angle plate-points]
  (map (partial pronate-plate pronation-angle)
       (map (partial tent-plate tenting-angle) plate-points))
  )

(defn translate-plate-row [translation row]
  (for [pair row]
    (vector
      (map + translation (first pair))
      (map + translation (second pair))
      )))

(defn translate-plate [translation points]
  (map (partial translate-plate-row translation) points))


;;;;; Testing

(def example [
              [
               [[0 0 0] [1 1 1]]
               [[0 0 0] [1 1 1]]
               [[0 0 0] [1 1 1]]
               ]
              [
               [[0 0 0] [1 1 1]]
               [[0 0 0] [1 1 1]]
               [[0 0 0] [1 1 1]]
               ]
              [
               [[0 0 0] [2 2 2]]
               [[0 0 0] [2 2 2]]
               [[0 0 0] [2 2 2]]
               ]
              [
               [[2 2 2] [1 1 1]]
               [[4 3 3] [1 1 1]]
               [[3 3 4] [1 1 1]]
               ]
              ])

(def offset 5)

(mapv #(mapv + [(- offset) offset 0] %) (first (first example)))
