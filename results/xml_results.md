# XML strategy evaluation

| Strategy | E1 | E2 (us) | E3 | F(V) | TP | FP | FN | TN |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| V1_XML_XSD_BASIC | 0.1750 | 859.0250 | 0.6500 | 0.8050 | 20 | 7 | 0 | 13 |
| V2_XML_XSD_STRICT | 0.0000 | 617.6000 | 1.0000 | 0.1438 | 20 | 0 | 0 | 20 |

Best strategy (default weights 0.5/0.2/0.3): `V2_XML_XSD_STRICT`

## Sensitivity analysis

| w1 | w2 | w3 | bestStrategy |
|---:|---:|---:|---|
| 0.5000 | 0.2000 | 0.3000 | `V2_XML_XSD_STRICT` |
| 0.6000 | 0.2000 | 0.2000 | `V2_XML_XSD_STRICT` |
| 0.4000 | 0.2000 | 0.4000 | `V2_XML_XSD_STRICT` |

## M/M/1 threshold (XML)

λ* = μ2 − μ1, μ = 1/E2 (E2 in µs, μ in s⁻¹): **455.0605** s⁻¹
