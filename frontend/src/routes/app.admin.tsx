import { createFileRoute } from "@tanstack/react-router";
import { ShieldCheck } from "lucide-react";
import { PageHeader, Placeholder } from "@/components/ui-kit";

export const Route = createFileRoute("/app/admin")({
  head: () => ({ meta: [{ title: "Admin — ForgeMind AI" }] }),
  component: () => (
    <div>
      <PageHeader title="Admin" description="Advanced administration surfaces." />
      <Placeholder
        icon={<ShieldCheck className="h-6 w-6" />}
        title="Admin console is coming"
        description="User management, workspace controls, audit logs, and system health — all coming soon."
      />
    </div>
  ),
});
