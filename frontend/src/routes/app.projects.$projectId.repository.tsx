import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import Editor from "@monaco-editor/react";
import axios from "axios";
import {
  ChevronDown,
  ChevronRight,
  Copy,
  File as FileIcon,
  FileText,
  Folder,
  FolderOpen,
  MessageSquare,
  Trash2,
  Upload,
} from "lucide-react";
import { Badge, Button, Card, EmptyState, Spinner } from "@/components/ui-kit";
import { repositoryService } from "@/services";
import { apiErrorMessage } from "@/lib/api-client";
import type { RepositoryTreeNode } from "@/types";

export const Route = createFileRoute("/app/projects/$projectId/repository")({
  component: RepositoryPage,
});

function RepositoryPage() {
  const { projectId } = useParams({ from: "/app/projects/$projectId" });
  const qc = useQueryClient();
  const [selectedFileId, setSelectedFileId] = useState<string | null>(null);

  const repoQ = useQuery({
    queryKey: ["repo", projectId],
    queryFn: () => repositoryService.detail(projectId),
    retry: (count, err) => (axios.isAxiosError(err) && err.response?.status === 404 ? false : count < 2),
  });

  const notFound =
    repoQ.isError &&
    axios.isAxiosError(repoQ.error) &&
    repoQ.error.response?.status === 404;

  const treeQ = useQuery({
    queryKey: ["repo", projectId, "tree"],
    queryFn: () => repositoryService.tree(projectId),
    enabled: !!repoQ.data,
  });

  const fileQ = useQuery({
    queryKey: ["repo", projectId, "file", selectedFileId],
    queryFn: () => repositoryService.file(projectId, selectedFileId!),
    enabled: !!selectedFileId,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["repo", projectId] });
  };

  const deleteMut = useMutation({
    mutationFn: () => repositoryService.remove(projectId),
    onSuccess: () => {
      toast.success("Repository deleted");
      setSelectedFileId(null);
      invalidate();
    },
    onError: (e) => toast.error(apiErrorMessage(e)),
  });

  if (repoQ.isLoading) return <div className="py-12 grid place-items-center"><Spinner /></div>;

  if (notFound || !repoQ.data) {
    return <UploadEmptyState projectId={projectId} onUploaded={invalidate} />;
  }

  const repo = repoQ.data;

  return (
    <div>
      <Card className="mb-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex-1 min-w-[200px]">
            <div className="text-sm font-medium truncate">{repo.originalFilename}</div>
            <div className="text-xs text-muted-foreground">
              {repo.totalFiles} files · {repo.totalFolders} folders · {formatBytes(repo.totalSizeBytes)}
              {repo.primaryLanguage && <> · Primary: {repo.primaryLanguage}</>}
            </div>
          </div>
          <StatusBadge status={repo.status} />
          <UploadButton projectId={projectId} onUploaded={invalidate} label="Re-upload" />
          <Button
            variant="danger"
            onClick={() => {
              if (confirm("Delete the uploaded repository?")) deleteMut.mutate();
            }}
          >
            <Trash2 className="h-4 w-4" /> Delete
          </Button>
        </div>
        {repo.parseError && (
          <div className="mt-4 text-xs text-destructive">Parse error: {repo.parseError}</div>
        )}
      </Card>

      {repo.status !== "READY" ? (
        <EmptyState
          title={repo.status === "PARSING" ? "Parsing your repository…" : "Parsing failed"}
          description={repo.status === "PARSING" ? "Give it a moment — this page will update." : "You can try uploading again."}
          icon={<Spinner />}
        />
      ) : (
        <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
          <Card className="p-3">
            <div className="text-xs uppercase tracking-wide text-muted-foreground px-2 py-1">Files</div>
            {treeQ.isLoading ? (
              <div className="py-8 grid place-items-center"><Spinner /></div>
            ) : (
              <div className="max-h-[70vh] overflow-y-auto">
                <FileTree
                  nodes={treeQ.data ?? []}
                  onSelect={(id) => setSelectedFileId(id)}
                  selectedId={selectedFileId}
                />
              </div>
            )}
          </Card>
          <Card className="p-0 overflow-hidden">
            {!selectedFileId ? (
              <div className="p-10 text-center text-sm text-muted-foreground">
                Select a file to view its contents.
              </div>
            ) : fileQ.isLoading ? (
              <div className="py-12 grid place-items-center"><Spinner /></div>
            ) : fileQ.data ? (
              <FileViewer projectId={projectId} file={fileQ.data} />
            ) : null}
          </Card>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }: { status: "PARSING" | "READY" | "FAILED" }) {
  if (status === "READY") return <Badge variant="success">Ready</Badge>;
  if (status === "PARSING") return <Badge variant="warning">Parsing</Badge>;
  return <Badge variant="danger">Failed</Badge>;
}

function UploadButton({
  projectId,
  onUploaded,
  label = "Upload ZIP",
}: {
  projectId: string;
  onUploaded: () => void;
  label?: string;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept=".zip"
        className="hidden"
        onChange={async (e) => {
          const file = e.target.files?.[0];
          if (!file) return;
          setBusy(true);
          try {
            await repositoryService.upload(projectId, file, true);
            toast.success("Repository uploaded");
            onUploaded();
          } catch (err) {
            toast.error(apiErrorMessage(err));
          } finally {
            setBusy(false);
            if (inputRef.current) inputRef.current.value = "";
          }
        }}
      />
      <Button variant="secondary" loading={busy} onClick={() => inputRef.current?.click()}>
        <Upload className="h-4 w-4" /> {label}
      </Button>
    </>
  );
}

function UploadEmptyState({ projectId, onUploaded }: { projectId: string; onUploaded: () => void }) {
  return (
    <EmptyState
      icon={<Upload className="h-6 w-6 text-brand" />}
      title="Upload your repository"
      description="Upload a ZIP of your codebase to explore files and chat with the AI about it."
      action={<UploadButton projectId={projectId} onUploaded={onUploaded} />}
    />
  );
}

function FileTree({
  nodes,
  onSelect,
  selectedId,
  depth = 0,
}: {
  nodes: RepositoryTreeNode[];
  onSelect: (id: string) => void;
  selectedId: string | null;
  depth?: number;
}) {
  return (
    <ul className="text-sm">
      {nodes.map((n) => (
        <TreeNode key={n.path} node={n} onSelect={onSelect} selectedId={selectedId} depth={depth} />
      ))}
    </ul>
  );
}

function TreeNode({
  node,
  onSelect,
  selectedId,
  depth,
}: {
  node: RepositoryTreeNode;
  onSelect: (id: string) => void;
  selectedId: string | null;
  depth: number;
}) {
  const [open, setOpen] = useState(depth < 1);
  const pad = { paddingLeft: 8 + depth * 12 };

  if (node.type === "FOLDER") {
    return (
      <li>
        <button
          className="w-full flex items-center gap-1 py-1 px-1 rounded hover:bg-muted/40 text-left"
          style={pad}
          onClick={() => setOpen((o) => !o)}
        >
          {open ? <ChevronDown className="h-3 w-3 shrink-0" /> : <ChevronRight className="h-3 w-3 shrink-0" />}
          {open ? <FolderOpen className="h-3.5 w-3.5 text-brand shrink-0" /> : <Folder className="h-3.5 w-3.5 text-brand shrink-0" />}
          <span className="truncate">{node.name}</span>
        </button>
        {open && node.children.length > 0 && (
          <FileTree nodes={node.children} onSelect={onSelect} selectedId={selectedId} depth={depth + 1} />
        )}
      </li>
    );
  }
  const active = selectedId === node.id;
  return (
    <li>
      <button
        className={`w-full flex items-center gap-1 py-1 px-1 rounded text-left ${
          active ? "bg-brand-soft ring-brand" : "hover:bg-muted/40"
        }`}
        style={pad}
        onClick={() => node.id && onSelect(node.id)}
        disabled={!node.id}
      >
        <span className="w-3 shrink-0" />
        <FileIcon className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
        <span className="truncate">{node.name}</span>
      </button>
    </li>
  );
}

function FileViewer({
  projectId,
  file,
}: {
  projectId: string;
  file: import("@/types").RepositoryFile;
}) {
  const language = useMemo(() => file.language?.toLowerCase() ?? file.extension ?? "plaintext", [file]);
  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 border-b border-border/60 bg-card/40">
        <div className="min-w-0">
          <div className="font-mono text-xs truncate">{file.path}</div>
          <div className="text-[10px] text-muted-foreground mt-0.5">
            {file.language ?? "Unknown"} · {formatBytes(file.sizeBytes)}
            {file.contentTruncated && <> · <span className="text-yellow-400">truncated</span></>}
            {file.binaryFile && <> · <span className="text-yellow-400">binary</span></>}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="ghost"
            onClick={() => {
              if (file.content != null) {
                navigator.clipboard.writeText(file.content);
                toast.success("Copied to clipboard");
              }
            }}
          >
            <Copy className="h-3.5 w-3.5" /> Copy
          </Button>
          <Link
            to="/app/projects/$projectId/ai"
            params={{ projectId }}
            search={{ fileId: file.id }}
          >
            <Button size="sm">
              <MessageSquare className="h-3.5 w-3.5" /> Ask AI about this file
            </Button>
          </Link>
        </div>
      </div>
      {file.binaryFile ? (
        <div className="p-10 text-center text-sm text-muted-foreground">
          <FileText className="mx-auto h-8 w-8 mb-2" />
          Binary file cannot be previewed.
        </div>
      ) : (
        <div className="h-[65vh]">
          <Editor
            height="100%"
            theme="vs-dark"
            language={language}
            value={file.content ?? ""}
            options={{ readOnly: true, minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }}
          />
        </div>
      )}
    </div>
  );
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
