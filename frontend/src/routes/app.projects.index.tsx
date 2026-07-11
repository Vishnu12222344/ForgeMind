import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "sonner";
import {
  Archive,
  ArchiveRestore,
  FolderKanban,
  MoreVertical,
  Plus,
  Search,
  Star,
  StarOff,
  Trash2,
} from "lucide-react";
import { Badge, Button, Card, EmptyState, Input, PageHeader, Spinner } from "@/components/ui-kit";
import { projectService, type ProjectListParams } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";
import { formatDistanceToNow } from "date-fns";
import type { Project } from "@/types";

export const Route = createFileRoute("/app/projects/")({
  head: () => ({ meta: [{ title: "Projects — ForgeMind AI" }] }),
  component: ProjectsPage,
});

function ProjectsPage() {
  const [filters, setFilters] = useState<ProjectListParams>({ size: 20, sort: "updatedAt,desc" });
  const [search, setSearch] = useState("");
  const qc = useQueryClient();
  const navigate = useNavigate();

  const projectsQ = useQuery({
    queryKey: ["projects", filters, search],
    queryFn: () => projectService.list({ ...filters, search: search || undefined }),
  });

  const invalidate = () => qc.invalidateQueries({ queryKey: ["projects"] });

  const favMut = useMutation({
    mutationFn: (p: Project) => (p.favorite ? projectService.unfavorite(p.id) : projectService.favorite(p.id)),
    onSuccess: () => invalidate(),
    onError: (e) => toast.error(apiErrorMessage(e)),
  });
  const archiveMut = useMutation({
    mutationFn: (p: Project) => (p.archived ? projectService.unarchive(p.id) : projectService.archive(p.id)),
    onSuccess: () => invalidate(),
    onError: (e) => toast.error(apiErrorMessage(e)),
  });
  const deleteMut = useMutation({
    mutationFn: (id: string) => projectService.remove(id),
    onSuccess: () => {
      toast.success("Project deleted");
      invalidate();
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  const projects = projectsQ.data?.content ?? [];

  return (
    <div>
      <PageHeader
        title="Projects"
        description="All projects in your workspace."
        actions={
          <Button onClick={() => navigate({ to: "/app/projects/new" })}>
            <Plus className="h-4 w-4" /> New project
          </Button>
        }
      />

      <Card className="mb-6">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[200px]">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              className="pl-9"
              placeholder="Search projects…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            className="h-10 rounded-md bg-input/60 border border-border px-3 text-sm"
            value={filters.archived === undefined ? "active" : filters.archived ? "archived" : "all"}
            onChange={(e) => {
              const v = e.target.value;
              setFilters((f) => ({
                ...f,
                archived: v === "all" ? undefined : v === "archived",
              }));
            }}
          >
            <option value="active">Active</option>
            <option value="archived">Archived</option>
            <option value="all">All</option>
          </select>
          <select
            className="h-10 rounded-md bg-input/60 border border-border px-3 text-sm"
            value={filters.favorite === undefined ? "any" : filters.favorite ? "yes" : "no"}
            onChange={(e) => {
              const v = e.target.value;
              setFilters((f) => ({
                ...f,
                favorite: v === "any" ? undefined : v === "yes",
              }));
            }}
          >
            <option value="any">Any favorite</option>
            <option value="yes">Favorites only</option>
            <option value="no">Not favorites</option>
          </select>
          <select
            className="h-10 rounded-md bg-input/60 border border-border px-3 text-sm"
            value={filters.visibility ?? "any"}
            onChange={(e) => {
              const v = e.target.value;
              setFilters((f) => ({
                ...f,
                visibility: v === "any" ? undefined : (v as "PUBLIC" | "PRIVATE"),
              }));
            }}
          >
            <option value="any">Any visibility</option>
            <option value="PRIVATE">Private</option>
            <option value="PUBLIC">Public</option>
          </select>
        </div>
      </Card>

      {projectsQ.isLoading ? (
        <div className="py-16 grid place-items-center"><Spinner /></div>
      ) : projects.length === 0 ? (
        <EmptyState
          icon={<FolderKanban className="h-6 w-6 text-brand" />}
          title="No projects yet"
          description="Create your first project to start uploading repositories and chatting with the AI."
          action={
            <Link to="/app/projects/new">
              <Button><Plus className="h-4 w-4" /> New project</Button>
            </Link>
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {projects.map((p) => (
            <Card key={p.id} className="group relative hover:ring-brand transition">
              <div className="flex items-start justify-between gap-2">
                <Link
                  to="/app/projects/$projectId"
                  params={{ projectId: p.id }}
                  className="min-w-0 flex-1"
                >
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold truncate">{p.name}</h3>
                    {p.favorite && <Star className="h-3.5 w-3.5 text-yellow-400 fill-yellow-400" />}
                    {p.archived && <Badge variant="muted">Archived</Badge>}
                  </div>
                  <p className="mt-1 text-sm text-muted-foreground line-clamp-2 min-h-[2.5rem]">
                    {p.description || "No description"}
                  </p>
                </Link>
                <ProjectMenu
                  project={p}
                  onFavorite={() => favMut.mutate(p)}
                  onArchive={() => archiveMut.mutate(p)}
                  onDelete={() => {
                    if (confirm(`Delete “${p.name}”? This cannot be undone.`)) deleteMut.mutate(p.id);
                  }}
                />
              </div>
              <div className="mt-4 flex flex-wrap gap-1.5">
                {p.tags.slice(0, 4).map((t) => (
                  <Badge key={t} variant="muted">{t}</Badge>
                ))}
              </div>
              <div className="mt-4 flex items-center justify-between text-xs text-muted-foreground">
                <span>{p.visibility}</span>
                <span>updated {formatDistanceToNow(new Date(p.updatedAt), { addSuffix: true })}</span>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function ProjectMenu({
  project,
  onFavorite,
  onArchive,
  onDelete,
}: {
  project: Project;
  onFavorite: () => void;
  onArchive: () => void;
  onDelete: () => void;
}) {
  const [open, setOpen] = useState(false);
  return (
    <div className="relative">
      <button
        onClick={(e) => {
          e.preventDefault();
          setOpen((o) => !o);
        }}
        className="p-1.5 rounded-md hover:bg-muted/50 text-muted-foreground"
        aria-label="Project actions"
      >
        <MoreVertical className="h-4 w-4" />
      </button>
      {open && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />
          <div className="absolute right-0 mt-1 z-20 w-44 rounded-md border border-border bg-popover shadow-xl p-1 text-sm">
            <button
              onClick={() => { onFavorite(); setOpen(false); }}
              className="w-full flex items-center gap-2 px-2 py-1.5 rounded hover:bg-muted/60"
            >
              {project.favorite ? <StarOff className="h-4 w-4" /> : <Star className="h-4 w-4" />}
              {project.favorite ? "Unfavorite" : "Favorite"}
            </button>
            <button
              onClick={() => { onArchive(); setOpen(false); }}
              className="w-full flex items-center gap-2 px-2 py-1.5 rounded hover:bg-muted/60"
            >
              {project.archived ? <ArchiveRestore className="h-4 w-4" /> : <Archive className="h-4 w-4" />}
              {project.archived ? "Unarchive" : "Archive"}
            </button>
            <button
              onClick={() => { onDelete(); setOpen(false); }}
              className="w-full flex items-center gap-2 px-2 py-1.5 rounded hover:bg-destructive/20 text-destructive"
            >
              <Trash2 className="h-4 w-4" /> Delete
            </button>
          </div>
        </>
      )}
    </div>
  );
}
