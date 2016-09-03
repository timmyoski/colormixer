;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.

(def app-board [{:color [122 96 164], :mutable true, :neighbors {:right 1, :down 9}, :blend-directions ["left" "right" "up" "down"]} {:color [188 4 244], :mutable true, :neighbors {:left 0, :right 2, :down 10}, :blend-directions ["left" "right" "up" "down"]} {:color [55 223 77], :mutable true, :neighbors {:left 1, :right 3, :down 11}, :blend-directions ["left" "right" "up" "down"]} {:color [170 175 27], :mutable true, :neighbors {:left 2, :right 4, :down 12}, :blend-directions ["left" "right" "up" "down"]} {:color [59 217 78], :mutable true, :neighbors {:left 3, :right 5, :down 13}, :blend-directions ["left" "right" "up" "down"]} {:color [18 254 187], :mutable true, :neighbors {:left 4, :right 6, :down 14}, :blend-directions ["left" "right" "up" "down"]} {:color [4 57 106], :mutable true, :neighbors {:left 5, :right 7, :down 15}, :blend-directions ["left" "right" "up" "down"]} {:color [56 50 14], :mutable true, :neighbors {:left 6, :right 8, :down 16}, :blend-directions ["left" "right" "up" "down"]} {:color [186 238 107], :mutable true, :neighbors {:left 7, :down 17}, :blend-directions ["left" "right" "up" "down"]} {:color [29 31 177], :mutable true, :neighbors {:right 10, :up 0, :down 18}, :blend-directions ["left" "right" "up" "down"]} {:color [237 184 180], :mutable true, :neighbors {:left 9, :right 11, :up 1, :down 19}, :blend-directions ["left" "right" "up" "down"]} {:color [208 193 40], :mutable true, :neighbors {:left 10, :right 12, :up 2, :down 20}, :blend-directions ["left" "right" "up" "down"]} {:color [29 33 178], :mutable true, :neighbors {:left 11, :right 13, :up 3, :down 21}, :blend-directions ["left" "right" "up" "down"]} {:color [223 152 201], :mutable true, :neighbors {:left 12, :right 14, :up 4, :down 22}, :blend-directions ["left" "right" "up" "down"]} {:color [1 58 83], :mutable true, :neighbors {:left 13, :right 15, :up 5, :down 23}, :blend-directions ["left" "right" "up" "down"]} {:color [217 148 41], :mutable true, :neighbors {:left 14, :right 16, :up 6, :down 24}, :blend-directions ["left" "right" "up" "down"]} {:color [12 164 42], :mutable true, :neighbors {:left 15, :right 17, :up 7, :down 25}, :blend-directions ["left" "right" "up" "down"]} {:color [199 44 231], :mutable true, :neighbors {:left 16, :up 8, :down 26}, :blend-directions ["left" "right" "up" "down"]} {:color [182 69 45], :mutable true, :neighbors {:right 19, :up 9, :down 27}, :blend-directions ["left" "right" "up" "down"]} {:color [10 38 0], :mutable true, :neighbors {:left 18, :right 20, :up 10, :down 28}, :blend-directions ["left" "right" "up" "down"]} {:color [51 218 124], :mutable true, :neighbors {:left 19, :right 21, :up 11, :down 29}, :blend-directions ["left" "right" "up" "down"]} {:color [8 43 199], :mutable true, :neighbors {:left 20, :right 22, :up 12, :down 30}, :blend-directions ["left" "right" "up" "down"]} {:color [81 27 9], :mutable true, :neighbors {:left 21, :right 23, :up 13, :down 31}, :blend-directions ["left" "right" "up" "down"]} {:color [158 161 29], :mutable true, :neighbors {:left 22, :right 24, :up 14, :down 32}, :blend-directions ["left" "right" "up" "down"]} {:color [228 97 227], :mutable true, :neighbors {:left 23, :right 25, :up 15, :down 33}, :blend-directions ["left" "right" "up" "down"]} {:color [17 238 16], :mutable true, :neighbors {:left 24, :right 26, :up 16, :down 34}, :blend-directions ["left" "right" "up" "down"]} {:color [145 109 217], :mutable true, :neighbors {:left 25, :up 17, :down 35}, :blend-directions ["left" "right" "up" "down"]} {:color [68 232 30], :mutable true, :neighbors {:right 28, :up 18, :down 36}, :blend-directions ["left" "right" "up" "down"]} {:color [158 31 122], :mutable true, :neighbors {:left 27, :right 29, :up 19, :down 37}, :blend-directions ["left" "right" "up" "down"]} {:color [222 245 58], :mutable true, :neighbors {:left 28, :right 30, :up 20, :down 38}, :blend-directions ["left" "right" "up" "down"]} {:color [34 186 167], :mutable true, :neighbors {:left 29, :right 31, :up 21, :down 39}, :blend-directions ["left" "right" "up" "down"]} {:color [220 134 206], :mutable true, :neighbors {:left 30, :right 32, :up 22, :down 40}, :blend-directions ["left" "right" "up" "down"]} {:color [117 153 119], :mutable true, :neighbors {:left 31, :right 33, :up 23, :down 41}, :blend-directions ["left" "right" "up" "down"]} {:color [82 61 67], :mutable true, :neighbors {:left 32, :right 34, :up 24, :down 42}, :blend-directions ["left" "right" "up" "down"]} {:color [92 137 3], :mutable true, :neighbors {:left 33, :right 35, :up 25, :down 43}, :blend-directions ["left" "right" "up" "down"]} {:color [141 16 26], :mutable true, :neighbors {:left 34, :up 26, :down 44}, :blend-directions ["left" "right" "up" "down"]} {:color [122 88 199], :mutable true, :neighbors {:right 37, :up 27, :down 45}, :blend-directions ["left" "right" "up" "down"]} {:color [157 121 114], :mutable true, :neighbors {:left 36, :right 38, :up 28, :down 46}, :blend-directions ["left" "right" "up" "down"]} {:color [63 153 165], :mutable true, :neighbors {:left 37, :right 39, :up 29, :down 47}, :blend-directions ["left" "right" "up" "down"]} {:color [106 61 217], :mutable true, :neighbors {:left 38, :right 40, :up 30, :down 48}, :blend-directions ["left" "right" "up" "down"]} {:color [217 85 232], :mutable true, :neighbors {:left 39, :right 41, :up 31, :down 49}, :blend-directions ["left" "right" "up" "down"]} {:color [38 192 82], :mutable true, :neighbors {:left 40, :right 42, :up 32, :down 50}, :blend-directions ["left" "right" "up" "down"]} {:color [186 195 145], :mutable true, :neighbors {:left 41, :right 43, :up 33, :down 51}, :blend-directions ["left" "right" "up" "down"]} {:color [195 139 238], :mutable true, :neighbors {:left 42, :right 44, :up 34, :down 52}, :blend-directions ["left" "right" "up" "down"]} {:color [254 173 221], :mutable true, :neighbors {:left 43, :up 35, :down 53}, :blend-directions ["left" "right" "up" "down"]} {:color [52 234 203], :mutable true, :neighbors {:right 46, :up 36, :down 54}, :blend-directions ["left" "right" "up" "down"]} {:color [197 9 133], :mutable true, :neighbors {:left 45, :right 47, :up 37, :down 55}, :blend-directions ["left" "right" "up" "down"]} {:color [92 91 58], :mutable true, :neighbors {:left 46, :right 48, :up 38, :down 56}, :blend-directions ["left" "right" "up" "down"]} {:color [145 39 250], :mutable true, :neighbors {:left 47, :right 49, :up 39, :down 57}, :blend-directions ["left" "right" "up" "down"]} {:color [175 208 107], :mutable true, :neighbors {:left 48, :right 50, :up 40, :down 58}, :blend-directions ["left" "right" "up" "down"]} {:color [165 53 28], :mutable true, :neighbors {:left 49, :right 51, :up 41, :down 59}, :blend-directions ["left" "right" "up" "down"]} {:color [88 233 70], :mutable true, :neighbors {:left 50, :right 52, :up 42, :down 60}, :blend-directions ["left" "right" "up" "down"]} {:color [141 106 115], :mutable true, :neighbors {:left 51, :right 53, :up 43, :down 61}, :blend-directions ["left" "right" "up" "down"]} {:color [28 57 7], :mutable true, :neighbors {:left 52, :up 44, :down 62}, :blend-directions ["left" "right" "up" "down"]} {:color [62 73 181], :mutable true, :neighbors {:right 55, :up 45, :down 63}, :blend-directions ["left" "right" "up" "down"]} {:color [115 122 58], :mutable true, :neighbors {:left 54, :right 56, :up 46, :down 64}, :blend-directions ["left" "right" "up" "down"]} {:color [200 27 110], :mutable true, :neighbors {:left 55, :right 57, :up 47, :down 65}, :blend-directions ["left" "right" "up" "down"]} {:color [247 8 13], :mutable true, :neighbors {:left 56, :right 58, :up 48, :down 66}, :blend-directions ["left" "right" "up" "down"]} {:color [123 164 227], :mutable true, :neighbors {:left 57, :right 59, :up 49, :down 67}, :blend-directions ["left" "right" "up" "down"]} {:color [110 101 122], :mutable true, :neighbors {:left 58, :right 60, :up 50, :down 68}, :blend-directions ["left" "right" "up" "down"]} {:color [181 44 175], :mutable true, :neighbors {:left 59, :right 61, :up 51, :down 69}, :blend-directions ["left" "right" "up" "down"]} {:color [163 127 173], :mutable true, :neighbors {:left 60, :right 62, :up 52, :down 70}, :blend-directions ["left" "right" "up" "down"]} {:color [143 76 213], :mutable true, :neighbors {:left 61, :up 53, :down 71}, :blend-directions ["left" "right" "up" "down"]} {:color [90 51 120], :mutable true, :neighbors {:right 64, :up 54, :down 72}, :blend-directions ["left" "right" "up" "down"]} {:color [169 194 149], :mutable true, :neighbors {:left 63, :right 65, :up 55, :down 73}, :blend-directions ["left" "right" "up" "down"]} {:color [97 236 106], :mutable true, :neighbors {:left 64, :right 66, :up 56, :down 74}, :blend-directions ["left" "right" "up" "down"]} {:color [10 170 215], :mutable true, :neighbors {:left 65, :right 67, :up 57, :down 75}, :blend-directions ["left" "right" "up" "down"]} {:color [100 122 160], :mutable true, :neighbors {:left 66, :right 68, :up 58, :down 76}, :blend-directions ["left" "right" "up" "down"]} {:color [162 33 181], :mutable true, :neighbors {:left 67, :right 69, :up 59, :down 77}, :blend-directions ["left" "right" "up" "down"]} {:color [240 40 69], :mutable true, :neighbors {:left 68, :right 70, :up 60, :down 78}, :blend-directions ["left" "right" "up" "down"]} {:color [126 58 39], :mutable true, :neighbors {:left 69, :right 71, :up 61, :down 79}, :blend-directions ["left" "right" "up" "down"]} {:color [220 75 66], :mutable true, :neighbors {:left 70, :up 62}, :blend-directions ["left" "right" "up" "down"]}])

(defn num-board [a-board] (map #(:color %) a-board))

(def n-board (num-board app-board))

;; (defn lighten [brd]
;;   (map apply dec ))

(def e [33 44 55])


(defn inc-board [block-board]
  (vec (map #(assoc-in (:color %) (map inc (:color %))) block-board)))



[[3] [4] [555] e]

(def eeboard [{:color [1 1 1]
               :iam "dumb"}
              {:color [22 22 22]
               :whoa ["it" "works"]}
              {:color [33 33 33]}])

(map #(assoc-in % [:color] (vec (map inc (:color %)))) eeboard)

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

;; (symbol (str "r" "r"))






