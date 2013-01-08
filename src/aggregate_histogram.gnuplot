set terminal postscript eps color "Sans" 8 solid
set output "aggregate_histogram.eps"
set style histogram errorbars
set style data histograms
set xtic rotate by -90 scale 0
plot "aggregate_histogram.txt" using 2:3:xtic(1) title col, "aggregate_histogram.txt" using 4:5:xtic(1) title col, "aggregate_histogram.txt" using 6:7:xtic(1) title col