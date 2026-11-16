use bernoulli_ratio_oracle::{row, RATIOS, WORDS};
fn main() {
    for (n, d) in RATIOS {
        let (bits, values, draws) = row(n, d);
        let words = WORDS
            .iter()
            .map(|x| format!("\"{}\"", x))
            .collect::<Vec<_>>()
            .join(",");
        let values = values
            .iter()
            .map(|x| x.to_string())
            .collect::<Vec<_>>()
            .join(",");
        let draws = draws
            .iter()
            .map(|x| x.to_string())
            .collect::<Vec<_>>()
            .join(",");
        println!(
            "{{\"n\":{},\"d\":{},\"pBits\":\"{}\",\"words\":[{}],\"values\":[{}],\"draws\":[{}]}}",
            n, d, bits, words, values, draws
        );
    }
}
