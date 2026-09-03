import React, { useState } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { paymentService } from '../../services/paymentService';
import { CreditCard, CheckCircle, ShieldCheck } from 'lucide-react';

const PaymentPage = () => {
  const { bookingId } = useParams();
  const [searchParams] = useSearchParams();
  const amount = searchParams.get('amount') || '75.00';
  const navigate = useNavigate();

  const [provider, setProvider] = useState('MOCK');
  const [loading, setLoading] = useState(false);

  const handlePay = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await paymentService.processPayment({
        bookingId,
        amount: parseFloat(amount),
        currency: 'USD',
        paymentProvider: provider
      });
      alert('Payment successful! Transaction Ref: ' + res.data?.transactionReference);
      navigate('/bookings');
    } catch (err) {
      alert('Payment failed: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-md mx-auto card p-4 border-0 shadow-sm rounded-4">
        <h4 className="fw-bold mb-3 d-flex align-items-center gap-2">
          <CreditCard className="text-primary" size={24} /> Complete Payment
        </h4>

        <div className="p-3 bg-light rounded-3 mb-4">
          <div className="d-flex justify-content-between small text-muted mb-1">
            <span>Booking Reference:</span>
            <span className="fw-bold text-dark">{bookingId}</span>
          </div>
          <div className="d-flex justify-content-between fs-5 fw-bold text-dark">
            <span>Total Payable:</span>
            <span className="text-success">${amount}</span>
          </div>
        </div>

        <form onSubmit={handlePay}>
          <div className="mb-4">
            <label className="form-label small fw-semibold">Payment Gateway Provider</label>
            <select className="form-select" value={provider} onChange={e => setProvider(e.target.value)}>
              <option value="MOCK">Mock Provider (Instant Test Success)</option>
              <option value="Stripe">Stripe Gateway Abstraction</option>
              <option value="PayHere">PayHere Gateway Abstraction</option>
            </select>
          </div>

          <div className="p-3 bg-success bg-opacity-10 text-success rounded-3 mb-4 small d-flex align-items-center gap-2">
            <ShieldCheck size={20} /> 256-Bit Encrypted Secure Checkout
          </div>

          <button type="submit" className="btn btn-success w-100 py-3 fw-bold" disabled={loading}>
            {loading ? 'Processing Payment...' : `Pay $${amount} Now`}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default PaymentPage;
