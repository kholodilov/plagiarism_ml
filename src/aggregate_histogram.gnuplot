set terminal postscript eps color "Sans" 8 solid
set output "aggregate_histogram.eps"
set style data histograms
set xtic rotate by -90 scale 0
plot "aggregate_histogram.txt" using 2:xtic(1) title col, '' using 4:xtic(1) title col, '' using 6:xtic(1) title col