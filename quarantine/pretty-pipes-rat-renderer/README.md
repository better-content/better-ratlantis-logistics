# Pretty Pipes rat renderer quarantine

This client-only experiment replaced each visible Pretty Pipes packet with a
miniature rat carrying the routed stack. It is quarantined because the 3D
integration did not render correctly in the pack.

The sources are deliberately outside `src/`, and the mixin is deliberately
absent from `ratlantis_logistics.mixins.json`. Consequently they are neither
compiled nor registered at runtime, and Pretty Pipes retains its native item
renderer.

To revisit the experiment, move the Java sources back to their matching
packages under `src/main/java`, restore `PipeItemRenderMixin` to the mixin
configuration's client list, and validate it in a real client before release.
