import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from fpdf import FPDF
import os

# Load the CSV
file_path = "C:/Users/OM/Downloads/u37.csv"  # Make sure the file is in the same directory or update the path
df = pd.read_csv(file_path)

# Create output folder
os.makedirs("plots", exist_ok=True)

# Detect column types
numeric_cols = df.select_dtypes(include=['int64', 'float64']).columns.tolist()
categorical_cols = df.select_dtypes(include=['object', 'category', 'bool']).columns.tolist()

# Summary statistics
summary = df.describe(include='all').transpose()
summary.to_csv("plots/summary_statistics.csv")

# Plot 1: Histograms for numeric columns
for col in numeric_cols:
    plt.figure(figsize=(6, 4))
    sns.histplot(df[col].dropna(), kde=True, bins=30)
    plt.title(f'Histogram: {col}')
    plt.tight_layout()
    plt.savefig(f"plots/hist_{col}.png")
    plt.close()

# Plot 2: Boxplots
for col in numeric_cols:
    plt.figure(figsize=(6, 4))
    sns.boxplot(x=df[col])
    plt.title(f'Boxplot: {col}')
    plt.tight_layout()
    plt.savefig(f"plots/box_{col}.png")
    plt.close()

# Plot 3: Correlation Heatmap
if len(numeric_cols) > 1:
    plt.figure(figsize=(10, 8))
    corr = df[numeric_cols].corr()
    sns.heatmap(corr, annot=True, cmap="coolwarm", fmt=".2f")
    plt.title('Correlation Heatmap')
    plt.tight_layout()
    plt.savefig("plots/correlation_heatmap.png")
    plt.close()

# Plot 4: Pairplot
if len(numeric_cols) > 1:
    sns.pairplot(df[numeric_cols].dropna())
    plt.savefig("plots/pairplot.png")
    plt.close()

# Plot 5: Bar plots for categoricals
for col in categorical_cols:
    plt.figure(figsize=(8, 4))
    df[col].value_counts().plot(kind='bar')
    plt.title(f'Bar Plot: {col}')
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(f"plots/bar_{col}.png")
    plt.close()

# Plot 6: Line plot (if index-like or time column exists)
df_reset = df.reset_index()
for col in numeric_cols:
    plt.figure(figsize=(8, 4))
    plt.plot(df_reset.index, df_reset[col])
    plt.title(f'Line Plot: {col} over Index')
    plt.xlabel('Index')
    plt.ylabel(col)
    plt.tight_layout()
    plt.savefig(f"plots/line_{col}.png")
    plt.close()

# Create PDF Report
pdf = FPDF()
pdf.set_auto_page_break(auto=True, margin=15)

# Add summary statistics
pdf.add_page()
pdf.set_font("Arial", "B", 14)
pdf.cell(200, 10, "Data Visualization Report", ln=True, align='C')
pdf.ln(10)
pdf.set_font("Arial", "", 12)
pdf.multi_cell(0, 10, "Summary statistics saved as 'summary_statistics.csv'.")

# Add plots to PDF
for file in sorted(os.listdir("plots")):
    if file.endswith(".png"):
        pdf.add_page()
        pdf.image(f"plots/{file}", x=10, y=20, w=180)

# Save PDF
pdf.output("data_visualization_report.pdf")

print("✅ Report generated: data_visualization_report.pdf")
