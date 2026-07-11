import { createFileRoute } from "@tanstack/react-router";
import { BookOpen } from "lucide-react";
import { PageHeader, Placeholder } from "@/components/ui-kit";

export const Route = createFileRoute("/app/documentation")({
  head: () => ({ meta: [{ title: "Documentation — ForgeMind AI" }] }),
  component: () => (
    <div>
      <PageHeader title="Documentation" description="Auto-generate READMEs and architecture docs from your repositories." />
      <Placeholder
        icon={<BookOpen className="h-6 w-6" />}
        title="Documentation generation is on the way"
        description="Soon you'll be able to generate structured project docs, API references, and onboarding guides directly from an uploaded repository."
      />
    </div>
  ),
});
