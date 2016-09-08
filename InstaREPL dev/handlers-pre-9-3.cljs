;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.

(defn init-ctrl-panel []
  {:mouse {:1 {:pressed false}}
   :keyboard {
              " "  {:code "Space" :key " " :pressed false
                    :f-pressed (fn [state e board-cur]  ;; leave a-key included? ;; if already derefed/accessed var val  ;; efficient to just keep passing it?
                                 (let [new-board ()]

                                      (do
                                        (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it blends the entire board")
                                        ;; (blend!nn-all state @state 10)
                                        )))}

              "."  {:code "Period" :key "." :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [view-info (meta @board-cur)
                                       new-board (with-meta
                                                   (prn ;;inc-ed-board
                                                     @board-cur) view-info)] ;; if using this do this somewhere else
                                         (do (prn "got to lighten from the key [" (.-code e) "] in ctrl-panel" "---> it lightens the entire board")
                                             (reset! board-cur new-board))))}

              ","  {:code "Comma" :key "," :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [view-info (meta @board-cur)
                                       new-board (with-meta (prn
                                                              ;;dec-ed-board
                                                              @board-cur) view-info)] ;; if using this do this somewhere else
                                         (do (prn "got to darken from the key [" (.-code e) "] in ctrl-panel" "---> it lightens the entire board")
                                             (reset! board-cur new-board))))}

              "r" {:code "KeyR" :key "r" :pressed false
                   :f-pressed (fn [state e board-cur]  ;; DO - always just pass round state/global and deref when get approp level --> key-cursor at key-handler
                                (let [view-info (meta @board-cur) ;; is this the new $$$$ jackpot?  view/model info as meta (view is always meta?)  (globals/constants always meta?)
                                      board-dim (:board-dimensions view-info)
                                      new-board (with-meta (prn
                                                             ;;new-board-refactor
                                                             board-dim) view-info)]
                                (do  (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it resets the board-colors")
                                     (reset! board-cur new-board))))}
              }})

(def panel (init-ctrl-panel))










(defn get-key-f [a-key ctrl-panel]
  (str (get-in ctrl-panel [:keyboard a-key :f-pressed])))


(get-key-f " " panel)


(defn test-f [& args]
  (prn args))


(test-f 3 4 5)

(map #(+ % 5) [44 55 66])





;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.
(def ex-board
  [{:color [13 41 252], :mutable true, :neighbors {:right 1, :down 2}, :blend-directions ["left" "right" "up" "down"]}
               {:color [69 78 45], :mutable true, :neighbors {:left 0, :right 2, :down 0}, :blend-directions ["left" "right" "up" "down"]}
 {:color [187 14 22], :mutable true, :neighbors {:left 1, :right 3, :down 0}, :blend-directions ["left" "right" "up" "do
wn"]} {:color [6 152 197], :mutable true, :neighbors {:left 2, :right 0, :down 0}, :blend-directions ["left" "right" "up
" "down"]} {:color [250 37 4], :mutable true, :neighbors {:left 3, :right 2, :down 1}, :blend-directions ["left" "right"]}])

(defn avg-colors [ & color-vecs ] ;;if supply 1 val vecs ([255]) or 2 val vecs ([255 255]) will return avg of only r or rg (no b) respectively
  (vec (map #(int (/ % (count color-vecs))) (vec (apply map + color-vecs)))));;;;;;;;;JANKY INT rounding

(defn weight-by [n-times prim-color color-vecs];;returns a new [vec of [co lor vecs]] with ratio of
  (vec (concat (repeat (* n-times (count color-vecs)) prim-color);; bcolor/ncolor = n
               color-vecs)))

(defn blender [block board] ;;output new-color
  (let [neighbors-colors (map #(:color (board %)) (vals (:neighbors block)))
        weight-prim-color 10]
    (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))))

(def b (ex-board 0))

(blender b ex-board)

(map #(blender % ex-board) ex-board)


(defn blender-board [a-board]
 (vec
    (map #(assoc-in % [:color] (blender % a-board)) a-board)))

(blender-board ex-board)

;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.
;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.


(def eeboard [{:color [1 1 1]
               :iam "dumb"}
              {:color [22 22 22]
               :whoa ["it" "works"]}
              {:color [33 33 33]}])

(defn map-f-board [a-function block-loc a-board]
  (vec
    (map
     #(assoc-in % [block-loc]
                    (vec (map a-function (block-loc %))))
     a-board)))

;; ;;disassoc fucntion from data structure

;; (defn swap!-board! [board-cursor new-board]
;;   (reset! board-cursor new-board))

(defn inc-ed-color-board [board]
  (map-f-board inc :color board))


(map-f-board inc :color eeboard)
(inc-ed-color-board (inc-ed-color-board eeboard))


(let [t "timmy"]
  (prn t))


  ;;proper names for these -> render render-rgb-input?
(defn render-rgb-input [color-type weighted-color-cor];onkeypress='return event.charCode >= 48 && event.charCode <= 57'>
  (let [rgb ["red" "green" "blue"] ;onenter-> sw focus to next?
        rgb-index (.indexOf rgb color-type)]
    ;add some sort of button that has a :on method that incs/decs the color input ;just have it dec the val, start that chain of derrefs/renders
      [:input {:class "rgb-input"
                     :type "text"
                     :max-length 3 ;; :on-key-press (fn [e] (if (< 47 (.-keyCode e) 58) (do (prn (.-keyCode e)) e)))
                     :name color-type
                     :placeholder color-type
                     :style {:border-color color-type}
                     :on-change (fn [e] (swap! weighted-color-cor assoc-in [rgb-index]
                                                                     (int (.-value (.-target e)))))}]))

(render-rgb-input "red" "cursor")









(defn make-keyword [ & inputs ]
  (keyword (apply str inputs)))
(make-keyword [4] "g" 5)
(make-keyword "div" "." "top-gui-wrapper")

(def t {:1 1 :2 2 :3 3})
(def at (atom t))

(get-in @at [:1])

(def testb [1 23 455])
(apply max testb)


;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.


(def eeboard [{:color [1 1 1]
               :iam "dumb"}
              {:color [22 22 22]
               :whoa ["it" "works"]}
              {:color [33 33 33]}])

(defn map-f-board [a-function block-loc a-board]
  (vec
    (map
     #(assoc-in % [block-loc]
                    (vec (map a-function (block-loc %))))
     a-board)))

;; ;;disassoc fucntion from data structure

;; (defn swap!-board! [board-cursor new-board]
;;   (reset! board-cursor new-board))

(defn inc-ed-color-board [board]
  (map-f-board inc :color board))


(map-f-board inc :color eeboard)

;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.
(def ex-board
  [{:color [13 41 252], :mutable true, :neighbors {:right 1, :down 2}, :blend-directions ["left" "right" "up" "down"]}
               {:color [69 78 45], :mutable true, :neighbors {:left 0, :right 2, :down 0}, :blend-directions ["left" "right" "up" "down"]}
 {:color [187 14 22], :mutable true, :neighbors {:left 1, :right 3, :down 0}, :blend-directions ["left" "right" "up" "do
wn"]} {:color [6 152 197], :mutable true, :neighbors {:left 2, :right 0, :down 0}, :blend-directions ["left" "right" "up
" "down"]} {:color [250 37 4], :mutable true, :neighbors {:left 3, :right 2, :down 1}, :blend-directions ["left" "right"]}])

(defn avg-colors [ & color-vecs ] ;;if supply 1 val vecs ([255]) or 2 val vecs ([255 255]) will return avg of only r or rg (no b) respectively
  (vec (map #(int (/ % (count color-vecs))) (vec (apply map + color-vecs)))));;;;;;;;;JANKY INT rounding

(defn weight-by [n-times prim-color color-vecs];;returns a new [vec of [co lor vecs]] with ratio of
  (vec (concat (repeat (* n-times (count color-vecs)) prim-color);; bcolor/ncolor = n
               color-vecs)))

(defn blender [block board] ;;output new-color
  (let [neighbors-colors (map #(:color (board %)) (vals (:neighbors block)))
        weight-prim-color 10]
    (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))))

(def b (ex-board 0))

(blender b ex-board)

(map #(blender % ex-board) ex-board)


(defn blender-board [a-board]
 (vec
    (map #(assoc-in % [:color] (blender % a-board)) a-board)))

(blender-board ex-board)



























