export type ReportCategory = 'SIMPLE' | 'INTERMEDIO' | 'AVANZADO';

export interface ReportSection {
  id: string;
  category: ReportCategory;
  title: string;
  description: string;
  headers: string[];
  rows: string[][];
}

export interface ReportSummary {
  from: string;
  to: string;
  generatedAt: string;
  sections: ReportSection[];
}
