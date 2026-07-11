# Deployment Guide

## Backend
1. Push to main
2. CI builds and tests
3. Docker image published to GHCR
4. Render deploy hook is triggered
5. Health check verifies /actuator/health

## Frontend
1. Push frontend changes
2. Vercel auto-deploys production

## Rollback
1. Identify previous healthy image tag
2. Redeploy previous Render release or previous Docker tag
3. Verify health endpoint