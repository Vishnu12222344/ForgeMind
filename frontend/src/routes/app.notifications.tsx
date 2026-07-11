import { createFileRoute } from "@tanstack/react-router";
import { Bell } from "lucide-react";
import { PageHeader, Placeholder } from "@/components/ui-kit";

export const Route = createFileRoute("/app/notifications")({
  head: () => ({ meta: [{ title: "Notifications — ForgeMind AI" }] }),
  component: () => (
    <div>
      <PageHeader title="Notifications" description="Stay on top of AI runs, repo parsing, and team activity." />
      <Placeholder
        icon={<Bell className="h-6 w-6" />}
        title="Notifications are coming soon"
        description="A unified inbox for project events, AI completions, and system alerts."
      />
    </div>
  ),
});
