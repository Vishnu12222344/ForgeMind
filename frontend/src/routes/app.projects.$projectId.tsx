import { createFileRoute, Link, Outlet, useParams, useRouterState } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { projectService } from "@/services";
import { Badge, Spinner } from "@/components/ui-kit";
import { ChevronLeft, FolderTree, MessageSquare, LayoutDashboard, Star, Archive } from "lucide-react";

export const Route = createFileRoute("/app/projects/$projectId")({
  component: ProjectShell,
});

function ProjectShell() {
  const { projectId } = useParams({ from: "/app/projects/$projectId" });
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const q = useQuery({
    queryKey: ["project", projectId],
    queryFn: () => projectService.detail(projectId),
  });

  const p = q.data;

  const tabs = [
    { to: `/app/projects/${projectId}`, exact: true, label: "Overview", icon: LayoutDashboard },
    { to: `/app/projects/${projectId}/repository`, label: "Repository", icon: FolderTree },
    { to: `/app/projects/${projectId}/ai`, label: "AI Assistant", icon: MessageSquare },
  ];

  return (
    <div>
      <Link to="/app/projects" className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground mb-4">
        <ChevronLeft className="h-3 w-3" /> Back to projects
      </Link>
      {q.isLoading ? (
        <div className="py-12 grid place-items-center"><Spinner /></div>
      ) : !p ? (
        <div className="text-sm text-muted-foreground">Project not found.</div>
      ) : (
        <>
          <div className="flex flex-wrap items-start justify-between gap-4 mb-2">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-semibold tracking-tight">{p.name}</h1>
                {p.favorite && <Star className="h-4 w-4 text-yellow-400 fill-yellow-400" />}
                {p.archived && <Badge variant="muted"><Archive className="h-3 w-3 mr-1 inline" />Archived</Badge>}
                <Badge variant="muted">{p.visibility}</Badge>
              </div>
              <p className="mt-1 text-sm text-muted-foreground max-w-2xl">{p.description || "No description"}</p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {p.tags.map((t) => (
                  <Badge key={t} variant="brand">{t}</Badge>
                ))}
              </div>
            </div>
          </div>
          <div className="mt-6 border-b border-border/60 flex gap-6">
            {tabs.map((t) => {
              const active = t.exact ? pathname === t.to : pathname.startsWith(t.to);
              const Icon = t.icon;
              return (
                <Link
                  key={t.to}
                  to={t.to}
                  className={`inline-flex items-center gap-2 px-1 py-3 text-sm border-b-2 -mb-px transition ${
                    active
                      ? "border-brand text-foreground"
                      : "border-transparent text-muted-foreground hover:text-foreground"
                  }`}
                >
                  <Icon className="h-4 w-4" /> {t.label}
                </Link>
              );
            })}
          </div>
          <div className="mt-6">
            <Outlet />
          </div>
        </>
      )}
    </div>
  );
}
