(ns mrkabuda.parts.key_hole_connectors
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :as m :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.plate_utils :refer :all]
            [unicode-math.core :refer :all]))

(defn- line [pair]
  (m/hull
    (m/translate (vec (first pair)) (m/sphere 0.1))
    (m/translate (vec (second pair)) (m/sphere 0.1))
    ))
  
(defn connect-pairs [pairs]
  (apply triangle-hulls (map line pairs)))

(defn horizontal-pairs [positions row column]
  (let [
        get-pair #(get (get positions %1) %2)
        ]
        [(get-pair row column) (get-pair row (inc column))]
    ))

(defn vertical-pairs [positions row column]
  (let [
        get-pair #(get (get positions %1) %2)
        ]
        [(get-pair row column) (get-pair (inc row) column)]
    ))

(defn connect-right
  "Connect segment to other rows (max one key per row)"
  [positions base-pairs]
  (let [ 
        rows (count positions)
        condition (fn 
                    [base-pairs current-pairs]
                    (let [
                          ;; comparing upper point of plate
                          base-pair-top (first (first base-pairs))
                          base-pair-bottom (first (second base-pairs))
                          current-pair-top (first (first current-pairs))
                          current-pair-bottom (first (second current-pairs))
                          ]
                      (and
                        ;; must be x greater than coresponding base pairs x 
                        (and 
                          (< (- (first current-pair-top) (first base-pair-top)) 18)
                          (> (first current-pair-top) (first base-pair-top))
                          (> (first current-pair-bottom) (first base-pair-bottom)))

                        ;; must have either y coords between outer y of base pairs
                        (or 
                          (and 
                            (>= (second current-pair-top) (second base-pair-bottom))
                            (<= (second current-pair-top) (second base-pair-top)))
                          (and 
                            (>= (second current-pair-bottom) (second base-pair-bottom))
                            (<= (second current-pair-bottom) (second base-pair-top)))
                          )
                        )
                      )
                    )
        matched-segment-for-row (fn 
                                  ;; get first in row to pass condition
                                  [positions row-index column-indexes] 
                                  (let [
                                        current-column (first column-indexes)
                                        current-pairs (vertical-pairs positions row-index current-column)
                                        ] 
                                    (if (condition base-pairs current-pairs) 
                                      current-pairs 
                                      (if (empty? (rest column-indexes)) 
                                        (vector)
                                        (recur positions row-index (rest column-indexes)))))
                                  )
        ]
    (map connect-pairs
         (for [
               row (range 0 rows 2)
               :let [
                     segment (matched-segment-for-row positions row (range 0 (count (nth positions row)) 2));; pass column-indexes for the row
                     ] 
               :when ((complement empty?) segment)
               ]
           (concat base-pairs segment)))
    )
  )

(defn condition-down
  [base-pairs current-pairs]
  (let [
        ;; comparing upper point of plate
        base-pair-left (first (first base-pairs))
        base-pair-right (first (second base-pairs))
        current-pair-left (first (first current-pairs))
        current-pair-right (first (second current-pairs))
        ]
    (and
      ;; must be greater than coresponding base pairs y 
      (and 
        ;; (< (- (second base-pair-left) (second current-pair-left)) 18)
        (< (second current-pair-left) (second base-pair-left))
        (< (second current-pair-right) (second base-pair-right)))

      ;; must have either x coords between outer x of base pairs
      (or 
        (and 
          (>= (first current-pair-left) (first base-pair-left))
          (<= (first current-pair-left) (first base-pair-right)))
        (and 
          (>= (first current-pair-right) (first base-pair-left))
          (<= (first current-pair-right) (first base-pair-right)))
        )
      )
    )
  )



(defn connect-down
  "Connect segment to next (down) rows (max one key per col)"
  [positions row-index base-pairs]
  (let [ 
        row (nth positions row-index)
        ]
    (map connect-pairs
         (for [
               column (range 0 (count row) 2)
               :let [current-pairs (horizontal-pairs positions row-index column)]
               :when (condition-down base-pairs current-pairs)
               ]
           (concat base-pairs current-pairs)))
    )
  )

(defn connect-down-gap
  "Connect segment to next (down) rows (max one key per col)"
  [positions row-index base-pairs base-gap-pairs]
  (let [ 
        row (nth positions row-index)
        columns (count row)
        ]
    (map connect-pairs
         (for [
               column (range 0 columns 2)
               :let [
                     current-pairs (horizontal-pairs positions row-index column)
                     current-gap-pairs (horizontal-pairs positions row-index (inc column))
                     base-gap-pair-left (first (first base-gap-pairs))
                     base-gap-pair-right (first (second base-gap-pairs))
                     current-gap-pair-left (first (first current-gap-pairs))
                     current-gap-pair-right (first (second current-gap-pairs))
                     ]
               :when (and 
                       (condition-down base-pairs current-pairs) 
                       (< (inc (inc column)) columns)
                       (and
                         (< (second current-gap-pair-left) (second base-gap-pair-left))
                         (< (second current-gap-pair-right) (second base-gap-pair-right)))
                       )
               ]
           (concat base-gap-pairs current-gap-pairs)
           ))
    )
  )

(defn connect-down-inclusive
  "Connect segment to next (down) rows (max one key per col)"
  [positions row-index base-pairs]
  (let [ 
        row (nth positions row-index)
        condition (fn 
                    [base-pairs current-pairs]
                    (let [
                          base-pair-left (first (first base-pairs))
                          base-pair-right (first (second base-pairs))
                          current-pair-left (first (first current-pairs))
                          current-pair-right (first (second current-pairs))
                          ]
                      (and 
                          (>= (first base-pair-left) (first current-pair-left))
                          (<= (first base-pair-right) (first current-pair-right)))
                      )
                    )
        ]
    (map connect-pairs
         (for [
               column (range 0 (count row) 2)
               :let [
                     current-pairs (horizontal-pairs positions row-index column)
                     [current-pair-left current-pair-right] current-pairs
                     [base-pair-left base-pair-right] base-pairs
                     ]
               :when (condition base-pairs current-pairs)
               ]
           (concat 
             base-pairs 
             (vector  
               (create-pair-at-x current-pair-left current-pair-right (first (first base-pair-left)))
               (create-pair-at-x current-pair-left current-pair-right (first (first base-pair-right)))
               )
             )))
    )
  )

(defn connect-down-inclusive-upward
  "Connect segment to next (down) rows (max one key per col)"
  [positions row-index base-pairs]
  (let [ 
        row (nth positions row-index)
        condition (fn 
                    [base-pairs current-pairs]
                    (let [
                          base-pair-left (first (first base-pairs))
                          base-pair-right (first (second base-pairs))
                          current-pair-left (first (first current-pairs))
                          current-pair-right (first (second current-pairs))
                          ]
                      (and 
                          (>= (first current-pair-left) (first base-pair-left))
                          (<= (first current-pair-right) (first base-pair-right)))
                      )
                    )
        ]
    (map connect-pairs
         (for [
               column (range 0 (count row) 2)
               :let [
                     current-pairs (horizontal-pairs positions row-index column)
                     [current-pair-left current-pair-right] current-pairs
                     [base-pair-left base-pair-right] base-pairs
                     ]
               :when (condition base-pairs current-pairs)
               ]
           (concat 
             current-pairs 
             (vector  
               (create-pair-at-x base-pair-left base-pair-right (first (first current-pair-left)))
               (create-pair-at-x base-pair-left base-pair-right (first (first current-pair-right)))
               )
             )))
    )
  )

(defn connect-right-all
  "Connect any pair that overlap with current cell on y-axix"
  [positions]
  (let [
        rows (count positions)
        ]
    ;; givs the right upper corner of key
    (for [row (range 0 rows 2)
          :let [columns (count (nth positions row))]
          column (range 1 (dec columns) 2)
          ]
      (connect-right positions (vertical-pairs positions row column))
      )
    )
  )

(defn connect-down-all
  "Connect any pair that overlap with current cell on y-axix"
  [positions]
  (let [
        rows (count positions)
        ]
    (concat
    ;; givs the bottom left corner of key
    (for [row (range 1 rows 2)
          :let [
                columns (count (nth positions row))
                next-row (inc row)
                ]
          :when (< next-row rows)
          column (range 0 (dec columns) 2)
          ]
      (connect-down positions next-row (horizontal-pairs positions row column))
      )

    (for [row (range 1 rows 2)
          :let [
                columns (count (nth positions row))
                next-row (inc row)
                ]
          column (range 0 (dec columns) 2)
          :when (and (< next-row rows) (< (inc (inc column)) columns)) ;; next gap
          ]
      (connect-down-gap positions next-row (horizontal-pairs positions row column) (horizontal-pairs positions row (inc column)))
      )
    
    (for [row (range 1 rows 2)
          :let [
                columns (count (nth positions row))
                next-row (inc row)
                ]
          column (range 0 (dec columns))
          :when (and (< next-row rows)) ;; next gap
          ]
      (connect-down-inclusive positions next-row (horizontal-pairs positions row column))
      )
    
    ;; I guess this isn't(?) needed when the upper row has the larger key (keep it for now because it is correct for mapping upward)
    ;; (for [row (range 1 rows 2)
    ;;       :let [
    ;;             columns (count (nth positions row))
    ;;             next-row (inc row)
    ;;             ]
    ;;       column (range 0 (dec columns))
    ;;       :when (and (< next-row rows)) ;; next gap
    ;;       ]
    ;;   (connect-down-inclusive-upward positions next-row (horizontal-pairs positions row column))
    ;;   )
    )
    )
  )

(defn connectors 
  "Intelligent connectors"
  [positions-seq]
  (let [
        positions (mapv vec positions-seq)
        rows (count positions)
        ]
    (m/union
           (connect-right-all positions)
           (connect-down-all positions)
           )))

(defn connect 
  "Connects a cell (row col) with points directly below it"
  [positions row column]
  (let [
        get-pair #(get (get positions %1) %2)
        ]
    (connect-pairs 
      (vector
        (get-pair row column) 
        (get-pair row (inc column)) 
        (get-pair (inc row) column) 
        (get-pair (inc row) (inc column))
        )
      )))

(defn connectors-old
  "Web connectors between key holes, must be a matrix"
  [positions-seq]
  (let [
        positions (mapv vec positions-seq)
        rows (count positions)
        ]
    (apply m/union
           (concat
             ;; Row connections
             (for [row (range 0 rows 2)
                   :let [columns (count (nth positions row))]
                   column (range 1 (dec columns) 2)]
               (connect positions row column))

             ;; Column connections
             (for [row (range 1 (dec rows) 2)
                   :let [columns (count (nth positions row))]
                   column (range 0 columns 2)]
               (connect positions row column))

             ;; Diagonal connections
             (for [row (range 1 (dec rows) 2)
                   :let [columns (count (nth positions row))]
                   column (range 1 (dec columns) 2)]
               (connect positions row column))
             ))))

(defn connect-all 
  "Connect all"
  [positions-seq]
  (let [
        positions (mapv vec positions-seq)
        rows (count positions)
        ]
    (apply m/union
           (concat
             (for [row (range 0 (dec rows))
                   :let [columns (count (nth positions row))]
                   column (range 0 (dec columns))]
               (connect positions row column))
             ))))
