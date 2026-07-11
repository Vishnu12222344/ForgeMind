import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { projectService, userService, workspaceService } from "@/services";
import { Card, PageHeader, Spinner, Badge, Button } from "@/components/ui-kit";
import { FolderKanban, Upload, MessageSquare, Sparkles, Star, Archive } from "lucide-react";
import { formatDistanceToNow } from "date-fns";

export const Route = createFileRoute("/app/dashboard")({
  head: () => ({ meta: [{ title: "Dashboard — ForgeMind AI" }] }),
  component: Dashboard,
});

function Dashboard() {
  const meQ = useQuery({ queryKey: ["me"], queryFn: () => userService.me() });
  const wsQ = useQuery({ queryKey: ["workspace"], queryFn: () => workspaceService.me() });
  const recentQ = useQuery({
    queryKey: ["projects", "recent"],
    queryFn: () => projectService.list({ size: 5, sort: "updatedAt,desc" }),
  });
  const countQ = useQuery({
    queryKey: ["projects", "count"],
    queryFn: () => projectService.list({ size: 1 }),
  });

  const user = meQ.data;
  const workspace = wsQ.data;
  const projects = recentQ.data?.content ?? [];
  const total = countQ.data?.totalElements ?? 0;

  return (
    <div>
      <PageHeader
        title={`Welcome${user?.fullName ? `, ${user.fullName.split(" ")[0]}` : ""}`}
        description="Your engineering workspace at a glance."
        actions={
          <Link to="/app/projects/new">
            <Button><FolderKanban className="h-4 w-4" /> New project</Button>
          </Link>
        }
      />

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4 mb-8">
        <Card>
          <div className="text-xs text-muted-foreground">Workspace</div>
          <div className="mt-2 text-lg font-semibold truncate">{workspace?.name ?? (wsQ.isLoading ? "…" : "—")}</div>
          {workspace?.personal && <Badge variant="brand" className="mt-2">Personal</Badge>}
        </Card>
        <Card>
          <div className="text-xs text-muted-foreground">Total projects</div>
          <div className="mt-2 text-3xl font-semibold text-brand">{countQ.isLoading ? "…" : total}</div>
        </Card>
        <Card>
          <div className="text-xs text-muted-foreground">AI usage</div>
          <div className="mt-2 text-3xl font-semibold">—</div>
          <p className="text-xs text-muted-foreground mt-1">Analytics coming soon</p>
        </Card>
        <Card>
          <div className="text-xs text-muted-foreground">Storage</div>
          <div className="mt-2 text-3xl font-semibold">—</div>
          <p className="text-xs text-muted-foreground mt-1">Analytics coming soon</p>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold">Recent projects</h2>
              <Link to="/app/projects" className="text-xs text-muted-foreground hover:text-foreground">
                View all →
              </Link>
            </div>
            {recentQ.isLoading ? (
              <div className="py-8 grid place-items-center"><Spinner /></div>
            ) : projects.length === 0 ? (
              <div className="text-sm text-muted-foreground py-8 text-center">
                No projects yet.{" "}
                <Link to="/app/projects/new" className="text-brand hover:underline">Create one</Link>.
              </div>
            ) : (
              <ul className="divide-y divide-border/60">
                {projects.map((p) => (
                  <li key={p.id} className="py-3 flex items-center justify-between gap-4">
                    <Link
                      to="/app/projects/$projectId"
                      params={{ projectId: p.id }}
                      className="min-w-0 flex-1 group"
                    >
                      <div className="flex items-center gap-2">
                        <span className="font-medium truncate group-hover:text-brand transition">{p.name}</span>
                        {p.favorite && <Star className="h-3 w-3 text-yellow-400 fill-yellow-400" />}
                        {p.archived && <Archive className="h-3 w-3 text-muted-foreground" />}
                      </div>
                      <div className="text-xs text-muted-foreground truncate">
                        {p.description || "No description"} · updated{" "}
                        {formatDistanceToNow(new Date(p.updatedAt), { addSuffix: true })}
                      </div>
                    </Link>
                    <Badge variant="muted">{p.visibility}</Badge>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
        <div>
          <Card>
            <h2 className="font-semibold mb-4">Quick actions</h2>
            <div className="space-y-2">
              <Link to="/app/projects/new">
                <Button variant="secondary" className="w-full justify-start">
                  <FolderKanban className="h-4 w-4" /> Create project
                </Button>
              </Link>
              <Link to="/app/projects">
                <Button variant="secondary" className="w-full justify-start">
                  <Upload className="h-4 w-4" /> Upload repository
                </Button>
              </Link>
              <Link to="/app/projects">
                <Button variant="secondary" className="w-full justify-start">
                  <MessageSquare className="h-4 w-4" /> Open AI assistant
                </Button>
              </Link>
            </div>
            <div className="mt-6 rounded-lg bg-brand-soft p-4 text-sm">
              <div className="flex items-center gap-2 font-medium">
                <Sparkles className="h-4 w-4 text-brand" /> Tip
              </div>
              <p className="mt-1 text-xs text-muted-foreground">
                Upload a ZIP of your repo, then ask the AI to explain the architecture.
              </p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
