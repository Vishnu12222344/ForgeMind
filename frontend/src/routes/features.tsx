import { createFileRoute } from "@tanstack/react-router";
import { PublicLayout } from "@/components/public-layout";
import { FolderKanban, FolderTree, Sparkles, FileText, Bell, BarChart3 } from "lucide-react";

export const Route = createFileRoute("/features")({
  head: () => ({
    meta: [
      { title: "Features — ForgeMind AI" },
      { name: "description", content: "Everything ForgeMind AI ships today and what's coming next." },
    ],
  }),
  component: FeaturesPage,
});

function FeaturesPage() {
  const features = [
    { icon: FolderKanban, title: "Project management", desc: "Create, tag, favorite, archive." },
    { icon: FolderTree, title: "Repository explorer", desc: "Upload ZIPs and browse the full file tree with a Monaco viewer." },
    { icon: Sparkles, title: "AI chat with code context", desc: "Grounded answers with referenced files and token accounting." },
    { icon: FileText, title: "Documentation generation", desc: "Coming soon." },
    { icon: Bell, title: "Notifications", desc: "Coming soon." },
    { icon: BarChart3, title: "Analytics", desc: "Coming soon." },
  ];
  return (
    <PublicLayout>
      <div className="mx-auto max-w-6xl px-6 py-20">
        <h1 className="text-4xl font-semibold tracking-tight">Features</h1>
        <p className="mt-3 text-muted-foreground max-w-2xl">A modern engineering workspace with an AI that reads your code.</p>
        <div className="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {features.map((f) => (
            <div key={f.title} className="rounded-xl border border-border/60 bg-card/60 p-6">
              <div className="h-10 w-10 rounded-lg bg-brand grid place-items-center shadow-glow">
                <f.icon className="h-5 w-5 text-white" />
              </div>
              <h3 className="mt-4 font-semibold">{f.title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </PublicLayout>
  );
}
