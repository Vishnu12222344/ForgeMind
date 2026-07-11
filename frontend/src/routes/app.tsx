import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { AppLayout } from "@/components/app-layout";
import { useAuthStore } from "@/store/auth-store";

export const Route = createFileRoute("/app")({
  beforeLoad: () => {
    // Auth store is hydrated in the root effect on the client. On the server / first paint,
    // fall back to reading localStorage synchronously to avoid a redirect flash.
    if (typeof window === "undefined") return;
    const state = useAuthStore.getState();
    if (!state.hydrated) state.hydrate();
    if (!useAuthStore.getState().isAuthenticated) {
      throw redirect({ to: "/login" });
    }
  },
  component: AppShell,
});

function AppShell() {
  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
}
