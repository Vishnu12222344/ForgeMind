import { createFileRoute } from "@tanstack/react-router";
import { PublicLayout } from "@/components/public-layout";

export const Route = createFileRoute("/about")({
  head: () => ({
    meta: [
      { title: "About — ForgeMind AI" },
      { name: "description", content: "About ForgeMind AI." },
    ],
  }),
  component: AboutPage,
});

function AboutPage() {
  return (
    <PublicLayout>
      <div className="mx-auto max-w-3xl px-6 py-20">
        <h1 className="text-4xl font-semibold tracking-tight">About ForgeMind AI</h1>
        <p className="mt-6 text-muted-foreground leading-relaxed">
          ForgeMind AI is an AI-powered engineering workspace. Upload a repository, explore every file,
          and pair with an assistant that grounds its answers in the code you're working on.
        </p>
      </div>
    </PublicLayout>
  );
}
