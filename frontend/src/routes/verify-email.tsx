import { createFileRoute, Link } from "@tanstack/react-router";
import { Logo } from "@/components/branding";
import { MailCheck } from "lucide-react";

export const Route = createFileRoute("/verify-email")({
  head: () => ({ meta: [{ title: "Verify email — ForgeMind AI" }] }),
  component: VerifyEmailPage,
});

function VerifyEmailPage() {
  return (
    <div className="min-h-screen grid place-items-center px-4">
      <div className="max-w-md w-full text-center">
        <div className="flex justify-center mb-6"><Logo /></div>
        <div className="rounded-2xl border border-border/60 bg-card/60 p-8">
          <div className="mx-auto h-12 w-12 rounded-xl bg-brand grid place-items-center shadow-glow">
            <MailCheck className="h-5 w-5 text-white" />
          </div>
          <h1 className="mt-4 text-xl font-semibold">Check your inbox</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            We've sent a verification link to your email. You can close this tab and follow the link.
          </p>
          <div className="mt-6"><Link to="/login" className="text-sm hover:underline">Back to login</Link></div>
        </div>
      </div>
    </div>
  );
}
