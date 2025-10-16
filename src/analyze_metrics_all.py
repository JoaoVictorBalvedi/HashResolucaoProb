import pandas as pd
import matplotlib.pyplot as plt
from pathlib import Path

padrao = Path("results/runs/metrics_padrao.csv")
bonus  = Path("results/runs/metrics_bonus.csv")

def load_csv(p):
    df = pd.read_csv(p)
    # normaliza cabeçalho: se não tiver run_id, cria
    if "run_id" not in df.columns:
        # assume 1 repetição
        df.insert(0, "run_id", 0)
    # normaliza tipos numéricos
    def numify(s):
        try:
            return pd.to_numeric(s)
        except Exception:
            return s
    df["value"] = df["value"].apply(numify)
    return df

dfs = []
if padrao.exists(): dfs.append(load_csv(padrao))
if bonus.exists():  dfs.append(load_csv(bonus))

if not dfs:
    raise SystemExit("Nenhum CSV encontrado em results/runs/.")

df = pd.concat(dfs, ignore_index=True)

# Converte times para ms
is_time = (df["metric"]=="time_ns")
df.loc[is_time, "metric"] = "time_ms"
df.loc[is_time, "value"]  = df.loc[is_time, "value"] / 1e6

# Converte memória para MB, se existir
has_mem = (df["metric"]=="memory_bytes").any()
if has_mem:
    mem_mask = (df["metric"]=="memory_bytes")
    df.loc[mem_mask, "metric"] = "memory_mb"
    df.loc[mem_mask, "value"]  = df.loc[mem_mask, "value"] / (1024*1024)

# Salva um CSV unificado “long”
Path("results/summary").mkdir(parents=True, exist_ok=True)
df.to_csv("results/summary/metrics_unificado.csv", index=False)

# Agrega média, desvio, min, max por combinação
stats = df.pivot_table(
    index=["table_type","table_m","hash_name","data_n","phase","metric"],
    values="value",
    aggfunc=["mean","std","min","max","count"]
).reset_index()
stats.columns = ["table_type","table_m","hash_name","data_n","phase","metric","mean","std","min","max","count"]
stats.to_csv("results/summary/stats_por_combinacao.csv", index=False)

# Salva recortes úteis
def save_slice(metric_name, outname):
    sub = stats[stats["metric"]==metric_name]
    if not sub.empty:
        sub.to_csv(f"results/summary/{outname}.csv", index=False)
        return sub
    return pd.DataFrame()

ins_ms   = save_slice("time_ms", "insert_time_ms")
sea_ms   = stats[(stats["metric"]=="time_ms") & (stats["phase"]=="search")]
sea_ms.to_csv("results/summary/search_time_ms.csv", index=False)

coll     = save_slice("collisions", "insert_collisions")
mem_mb   = save_slice("memory_mb", "insert_memory_mb")

# Top-3 cadeias do encadeamento
chains = df[(df["table_type"]=="chaining") & (df["phase"]=="structure") &
            (df["metric"].isin(["chain_top1","chain_top2","chain_top3"]))].copy()
if not chains.empty:
    chain_stats = chains.pivot_table(
        index=["table_type","table_m","hash_name","data_n","metric"],
        values="value",
        aggfunc=["mean","std","min","max","count"]
    ).reset_index()
    chain_stats.columns = ["table_type","table_m","hash_name","data_n","metric","mean","std","min","max","count"]
    chain_stats.to_csv("results/summary/chaining_top3_stats.csv", index=False)

# Gaps
gaps = df[(df["phase"]=="gaps") & (df["metric"].isin(["min","avg","max"]))].copy()
if not gaps.empty:
    gaps_stats = gaps.pivot_table(
        index=["table_type","table_m","hash_name","data_n","metric"],
        values="value", aggfunc=["mean","std","min","max","count"]
    ).reset_index()
    gaps_stats.columns = ["table_type","table_m","hash_name","data_n","metric","mean","std","min","max","count"]
    gaps_stats.to_csv("results/summary/gaps_stats.csv", index=False)

# Gráficos: tempo de inserção (média ± desvio) por tipo e n
def plot_with_error(sub, title, ylab, outfile):
    if sub.empty: return
    for ttype in sorted(sub["table_type"].unique()):
        for n in sorted(sub["data_n"].unique()):
            g = sub[(sub["table_type"]==ttype) & (sub["data_n"]==n)]
            if g.empty: continue
            fig = plt.figure()
            for m in sorted(g["table_m"].unique()):
                gm = g[g["table_m"]==m]
                # ordena hash_name para visual consistente
                gm = gm.sort_values("hash_name")
                plt.errorbar(gm["hash_name"], gm["mean"], yerr=gm["std"], marker="o", label=f"m={m}")
            plt.title(f"{title} — {ttype}, n={n}")
            plt.xlabel("hash")
            plt.ylabel(ylab)
            plt.legend()
            fig.savefig(f"results/summary/{outfile}_{ttype}_n{n}.png", bbox_inches="tight")
            plt.close(fig)

plot_with_error(ins_ms[ins_ms["phase"]=="insert"], "Insert time (ms)", "time (ms)", "plot_insert")
plot_with_error(sea_ms, "Search time (ms)", "time (ms)", "plot_search")
plot_with_error(coll, "Insert collisions", "collisions", "plot_collisions")
if not mem_mb.empty:
    plot_with_error(mem_mb, "Insert memory (MB)", "memory (MB)", "plot_memory")

print("OK: resultados em results/summary/")
