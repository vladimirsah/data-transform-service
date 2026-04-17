# JSON strategy evaluation

| Strategy | E1 | E2 (us) | E3 | F(V) | TP | FP | FN | TN |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| V3_JSON_SCHEMA_BASIC | 0.3750 | 77.4000 | 0.2500 | 0.8984 | 20 | 15 | 0 | 5 |
| V4_JSON_SCHEMA_STRICT | 0.0000 | 78.0250 | 0.7500 | 0.2000 | 20 | 0 | 0 | 20 |

Best strategy (default weights 0.5/0.2/0.3): `V4_JSON_SCHEMA_STRICT`

## Sensitivity analysis

| w1 | w2 | w3 | bestStrategy |
|---:|---:|---:|---|
| 0.5000 | 0.2000 | 0.3000 | `V4_JSON_SCHEMA_STRICT` |
| 0.6000 | 0.2000 | 0.2000 | `V4_JSON_SCHEMA_STRICT` |
| 0.4000 | 0.2000 | 0.4000 | `V4_JSON_SCHEMA_STRICT` |
