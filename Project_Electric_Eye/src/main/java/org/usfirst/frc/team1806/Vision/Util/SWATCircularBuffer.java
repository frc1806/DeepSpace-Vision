package org.usfirst.frc.team1806.Vision.Util;


public class SWATCircularBuffer{
    private double[] m_data;
    private int m_front;
    private int m_length;

    /**
     * Creates a circular buffer
     * @param size the size of the buffer
     */
    public SWATCircularBuffer(int size) {
        this.m_data = new double[size];

        for(int sizeItr = 0; sizeItr < this.m_data.length; ++sizeItr) {
            this.m_data[sizeItr] = 0.0D;
        }

    }

    /**
     * Gets the size of the circular buffer.
     * @return the size
     */
    public double size() {
        return (double)this.m_length;
    }

    /**
     *
     * @return the front value in the circular buffer
     */
    double getFirst() {
        return this.m_data[this.m_front];
    }

    /**
     *
     * @return the last value in the circular buffer
     */
    double getLast() {
        return this.m_length == 0 ? 0.0D : this.m_data[(this.m_front + this.m_length - 1) % this.m_data.length];
    }

    /**
     *
     * @param var1 adds a value to the front of the circular buffer
     */
    public void addFirst(double var1) {
        if (this.m_data.length != 0) {
            this.m_front = this.moduloDec(this.m_front);
            this.m_data[this.m_front] = var1;
            if (this.m_length < this.m_data.length) {
                ++this.m_length;
            }

        }
    }

    /**
     *
     * @param var1 adds a value to the back of the circular buffer
     */
    public void addLast(double var1) {
        if (this.m_data.length != 0) {
            this.m_data[(this.m_front + this.m_length) % this.m_data.length] = var1;
            if (this.m_length < this.m_data.length) {
                ++this.m_length;
            } else {
                this.m_front = this.moduloInc(this.m_front);
            }

        }
    }

    /**
     * Removes the first value in the circular buffer
     * @return the value that was removed
     */
    public double removeFirst() {
        if (this.m_length == 0) {
            return 0.0D;
        } else {
            double var1 = this.m_data[this.m_front];
            this.m_front = this.moduloInc(this.m_front);
            --this.m_length;
            return var1;
        }
    }

    /**
     * Removes the last value in the circular buffer
     * @return the value that was removed
     */
    public double removeLast() {
        if (this.m_length == 0) {
            return 0.0D;
        } else {
            --this.m_length;
            return this.m_data[(this.m_front + this.m_length) % this.m_data.length];
        }
    }

    void resize(int var1) {
        double[] var2 = new double[var1];
        this.m_length = Math.min(this.m_length, var1);

        for(int var3 = 0; var3 < this.m_length; ++var3) {
            var2[var3] = this.m_data[(this.m_front + var3) % this.m_data.length];
        }

        this.m_data = var2;
        this.m_front = 0;
    }


    /**
     * Empties the circular buffer
     */
    public void clear() {
        for(int var1 = 0; var1 < this.m_data.length; ++var1) {
            this.m_data[var1] = 0.0D;
        }

        this.m_front = 0;
        this.m_length = 0;
    }

    /**
     * Gets a value at the specified index
     * @param var1 the index at which to retrieve the value from
     * @return the value at the specified index
     */
    public double get(int var1) {
        return this.m_data[(this.m_front + var1) % this.m_data.length];
    }

    private int moduloInc(int var1) {
        return (var1 + 1) % this.m_data.length;
    }

    private int moduloDec(int var1) {
        return var1 == 0 ? this.m_data.length - 1 : var1 - 1;
    }

    /**
     * Gets the average of hte values in the circular buffer
     * @return the average value
     */
    public double getAverage(){
        double runningTotal = 0;
        for(int sizeItr = 0; sizeItr < size(); sizeItr++){
            runningTotal += this.m_data[sizeItr];
        }
        return runningTotal / size();
    }
}
