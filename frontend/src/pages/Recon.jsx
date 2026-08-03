import React, { useState } from "react";
import { withAuth } from "@components/withAuth.jsx";
import { api } from "@services/apiService.js";

function Recon() {

    const [loading, setLoading] = useState(false);
    const [jobId, setJobId] = useState("");
    const [status, setStatus] = useState("");
    const [results, setResults] = useState([]);
    const [error, setError] = useState("");

    async function runRecon() {

        setLoading(true);
        setError("");

        try {

          const response = await api.runRecon({
              from: "2026-08-01",
              to: "2026-08-31",
              counterpartyId: null
          });
            setJobId(response.jobId);
            setStatus(response.status);

            const rows = await api.reconResults(response.jobId);

            setResults(rows);

        } catch (e) {
            setError(e.message);
        }

        setLoading(false);
    }

    return (

        <section>

            <h2>Reconciliation</h2>

            <button
                onClick={runRecon}
                disabled={loading}
            >
                {loading ? "Running..." : "Run Reconciliation"}
            </button>

            {jobId &&
                <p>
                    <b>Job ID:</b> {jobId}
                </p>
            }

            {status &&
                <p>
                    <b>Status:</b> {status}
                </p>
            }

            {error &&
                <p style={{ color: "red" }}>
                    {error}
                </p>
            }

            <br />

            <table width="100%" border="1" cellPadding="8">

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Trade Ref</th>
                    <th>Status</th>
                    <th>Reason</th>
                </tr>
                </thead>

                <tbody>

                {results.length === 0 &&

                    <tr>
                        <td colSpan="4">
                            No reconciliation results
                        </td>
                    </tr>

                }

                {results.map((r, i) => (

                    <tr key={i}>
                        <td>{r.id}</td>
                        <td>{r.tradeRef}</td>
                        <td>{r.status}</td>
                        <td>{r.reason}</td>
                    </tr>

                ))}

                </tbody>

            </table>

        </section>

    );
}

export default withAuth(Recon);