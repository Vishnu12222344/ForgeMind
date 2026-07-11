import { createFileRoute } from "@tanstack/react-router";
import { BarChart3 } from "lucide-react";
import { PageHeader, Placeholder, Card } from "@/components/ui-kit";
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, CartesianGrid } from "recharts";

const sample = [
  { day: "Mon", tokens: 1240 },
  { day: "Tue", tokens: 2210 },
  { day: "Wed", tokens: 1780 },
  { day: "Thu", tokens: 3120 },
  { day: "Fri", tokens: 2560 },
  { day: "Sat", tokens: 980 },
  { day: "Sun", tokens: 1420 },
];

export const Route = createFileRoute("/app/analytics")({
  head: () => ({ meta: [{ title: "Analytics — ForgeMind AI" }] }),
  component: () => (
    <div>
      <PageHeader title="Analytics" description="Preview of your future AI and repo usage insights." />
      <Card className="mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold">AI token usage (sample)</h2>
          <span className="text-xs text-muted-foreground">Preview data</span>
        </div>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={sample}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="day" stroke="var(--color-muted-foreground)" fontSize={12} />
              <YAxis stroke="var(--color-muted-foreground)" fontSize={12} />
              <Tooltip
                contentStyle={{ background: "var(--color-card)", border: "1px solid var(--color-border)", borderRadius: 8 }}
              />
              <Bar dataKey="tokens" fill="oklch(0.68 0.20 285)" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
      <Placeholder
        icon={<BarChart3 className="h-6 w-6" />}
        title="Full analytics are on the way"
        description="Token spend, repository growth, active projects, AI response latency, and per-user breakdowns."
      />
    </div>
  ),
});
