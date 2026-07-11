import { createFileRoute, Link } from "@tanstack/react-router";
import {
  ArrowRight,
  FolderKanban,
  FolderTree,
  Sparkles,
  FileText,
  Upload,
  Search,
  MessageSquare,
} from "lucide-react";
import { PublicLayout } from "@/components/public-layout";

export const Route = createFileRoute("/")({ component: Landing });

function Landing() {
  return (
    <PublicLayout>
      {/* Hero */}
      <section className="relative overflow-hidden">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 opacity-40"
          style={{ background: "radial-gradient(60% 60% at 50% 0%, oklch(0.68 0.20 285 / 0.35), transparent 70%)" }}
        />
        <div className="relative mx-auto max-w-7xl px-6 pt-24 pb-20 text-center">
          <div className="inline-flex items-center gap-2 rounded-full border border-border/60 bg-card/60 px-3 py-1 text-xs text-muted-foreground">
            <Sparkles className="h-3 w-3 text-brand" /> AI engineering workspace, reimagined
          </div>
          <h1 className="mt-6 text-4xl sm:text-6xl font-semibold tracking-tight">
            <span className="text-brand">ForgeMind AI</span>
            <br /> ship code with an AI that reads your repo.
          </h1>
          <p className="mt-6 text-lg text-muted-foreground max-w-2xl mx-auto">
            Manage projects, upload your codebase, and chat with an assistant that grounds every answer
            in the files you point it at.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/register"
              className="inline-flex items-center gap-2 rounded-md bg-brand px-5 py-2.5 text-sm font-medium text-white shadow-glow"
            >
              Get started <ArrowRight className="h-4 w-4" />
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center gap-2 rounded-md border border-border px-5 py-2.5 text-sm text-foreground hover:bg-muted/40"
            >
              Log in
            </Link>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="mx-auto max-w-7xl px-6 py-20">
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {[
            { icon: FolderKanban, title: "Project management", desc: "Organize codebases, tag, favorite, archive." },
            { icon: FolderTree, title: "Repository explorer", desc: "Upload a ZIP and browse every file instantly." },
            { icon: Sparkles, title: "AI code assistant", desc: "Chat with your repo. Every answer is grounded." },
            { icon: FileText, title: "Documentation", desc: "Generate READMEs and architecture docs — soon." },
          ].map((f) => (
            <div key={f.title} className="rounded-xl border border-border/60 bg-card/60 p-6 hover:ring-brand transition">
              <div className="h-10 w-10 rounded-lg bg-brand grid place-items-center shadow-glow">
                <f.icon className="h-5 w-5 text-white" />
              </div>
              <h3 className="mt-4 font-semibold">{f.title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Workflow */}
      <section className="mx-auto max-w-7xl px-6 pb-24">
        <h2 className="text-3xl font-semibold text-center">How it works</h2>
        <div className="mt-12 grid gap-6 md:grid-cols-4">
          {[
            { icon: FolderKanban, step: "01", title: "Create a project" },
            { icon: Upload, step: "02", title: "Upload your repo" },
            { icon: Search, step: "03", title: "Explore files" },
            { icon: MessageSquare, step: "04", title: "Ask the AI" },
          ].map((s) => (
            <div key={s.step} className="rounded-xl border border-border/60 bg-card/60 p-6">
              <div className="flex items-center justify-between">
                <span className="text-xs text-muted-foreground">{s.step}</span>
                <s.icon className="h-4 w-4 text-brand" />
              </div>
              <h3 className="mt-6 font-semibold">{s.title}</h3>
            </div>
          ))}
        </div>
      </section>
    </PublicLayout>
  );
}
