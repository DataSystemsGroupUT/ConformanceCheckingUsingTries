// GPL License.
// Copyright: brurucy at protonmail dot ch
package spine;

import java.util.ArrayList;

class Vertebra<T> extends ArrayList<T> {
    public T max;

    public int ith(T key) {
        int low = 0;
        int mid;
        int high = this.size() - 1;

        while (low <= high) {
            mid = (low + high) >>> 1;
            Comparable<? super T> midVal = (Comparable<T>) this.get(mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else {
                return mid;
            }
        }

        return low;
    }

    public boolean has(T key) {
        int position = this.ith(key);
        if (position >= this.size()) {
            return false;
        }
        return this.get(position).equals(key);
    }

    @Override
    public boolean add(T key) {
        int position = this.ith(key);
        if (position >= this.size()) {
            this.max = key;
            return super.add(key);
        } else {
            Comparable<? super T> candidate = (Comparable<T>) this.get(position);
            int cmp = candidate.compareTo(key);
            if (cmp != 0) {
                this.add(position, key);
                int maxCmp = candidate.compareTo(this.max);
                if (maxCmp > 0) {
                    this.max = key;
                }
                return true;
            }
            return false;
        }
    }

    public boolean delete(T key) {
        int position = this.ith(key);
        if (position >= this.size()) {
            return false;
        }
        Comparable<? super T> candidate = (Comparable<T>) this.get(position);
        int cmp = candidate.compareTo(key);
        if (cmp == 0) {
            int maxCmp = candidate.compareTo(this.max);
            if (maxCmp == 0) {
                this.max = this.get(this.size() - 1);
            }
            this.remove(position);
            return true;
        }
        return false;
    }

}

class Cord {
    public int[] innerStructure;
    private int mostSignificantBit(int key) {
        int result = key;

        result |= result >> 1;
        result |= result >> 2;
        result |= result >> 4;
        result |= result >> 8;
        result |= result >> 16;
        result |= result >> 32;

        return result - (result >> 1);
    }

    private int leastSignificantBit(int key) {
        return key & -key;
    }

    public Cord(int[] lengthArray) {
        int length = lengthArray.length;
        this.innerStructure = new int[length];
        this.innerStructure[0] = lengthArray[0];
        for (int i = 1; i < length; i++) {
            this.innerStructure[i] = this.innerStructure[i - 1] + lengthArray[i];
        }
        for (int i = length - 1; i > 0; i--) {
            int lowerBound = (i & (i + 1)) - 1;
            if (lowerBound >= 0) {
                this.innerStructure[i] -= this.innerStructure[lowerBound];
            }
        }
    }

    public<T> Cord(ArrayList<Vertebra<T>> spine) {
        int length = spine.size();
        this.innerStructure = new int[length];
        this.innerStructure[0] = spine.get(0).size();
        for (int i = 1; i < length; i++) {
            this.innerStructure[i] = this.innerStructure[i - 1] + spine.get(i).size();
        }
        for (int i = length - 1; i > 0; i--) {
            int lowerBound = (i & (i + 1)) - 1;
            if (lowerBound >= 0) {
                this.innerStructure[i] -= this.innerStructure[lowerBound];
            }
        }
    }

    public int prefixSum(int ith) {
        int sum = 0;
        int i = ith;
        while (i > 0) {
            sum += this.innerStructure[i - 1];
            i &= i - 1;
        }
        return sum;
    }

    public void increaseLength(int ith) {
        int length = this.innerStructure.length;
        int i = ith;
        while (i < length) {
            this.innerStructure[i] += 1;
            i |= i + 1;
        }

    }

    public void decreaseLength(int ith) {
        int length = this.innerStructure.length;
        int i = ith;
        while (i < length) {
            this.innerStructure[i] -= 1;
            i |= i + 1;
        }
    }

    public int indexOf(int ith) {
        int length = this.innerStructure.length;
        int high = mostSignificantBit(length) * 2;
        int mid = 0;

        while (high != mid) {
            int lsb = leastSignificantBit(high);
            if (high <= length && this.innerStructure[high - 1] <= ith) {
                ith -= this.innerStructure[high - 1];
                mid = high;
                high += lsb / 2;
            } else {
                high += lsb / 2 - lsb;
            }
        }
        return mid;
    }
}

class IntegerTuple {
    public int k;
    public int v;

    public IntegerTuple(int k, int v) {
        this.k = k;
        this.v = v;
    }
}

/* Spine is a sorted set data structure. It is a one-level B-Tree without any pointers.
 * */
public class Spine<T extends Comparable<T>> {
    public ArrayList<Vertebra<T>> vertebrae;
    private Cord cord;
    private int length;
    public int vertebraSize;

    /* Initializes the spine with a vertebra size.
     * @param vertebraSize should be set to 1024 unless you have a *very* good reason.
     * */
    public Spine(int vertebraSize) {
        int[] emptyArray = {0};
        this.cord = new Cord(emptyArray);
        this.vertebrae = new ArrayList<>();
        this.vertebrae.add(new Vertebra<>());
        this.vertebraSize = vertebraSize;
        this.length = 0;
    }

    public int size() {
        return this.length;
    }

    public void clear() {
        int[] emptyArray = {0};
        this.cord = new Cord(emptyArray);
        this.vertebrae = new ArrayList<>();
        this.vertebrae.add(new Vertebra<>());
        this.length = 0;
    }

    private void buildCord() {
        this.cord = new Cord(this.vertebrae);
    }

    private void balance(int firstLevelIndex) {
        Vertebra<T> targetVertebra = this.vertebrae.get(firstLevelIndex);
        int firstLevelIndexLength = targetVertebra.size();
        int half = firstLevelIndexLength / 2;
        Vertebra<T> newVertebra = new Vertebra<>();
        Vertebra<T> oldVertebra = new Vertebra<>();
        for (int i = 0; i < firstLevelIndexLength; i++) {
            if (i < half) {
                oldVertebra.add(targetVertebra.get(i));
            } else {
                newVertebra.add(targetVertebra.get(i));
            }
        }
        this.vertebrae.set(firstLevelIndex, oldVertebra);
        this.vertebrae.add(firstLevelIndex + 1, newVertebra);
        this.buildCord();
    }

    private int spineIndexOf(T key) {
        int low = 0;
        int mid;
        int high = this.vertebrae.size() - 1;

        while (low <= high) {
            mid = (low + high) >>> 1;
            Comparable<? super T> midVal = this.vertebrae.get(mid).max;
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else {
                return mid;
            }
        }

        return low;
    }

    /* Adds a key of type T and retains sortedness.
     * @param key the key to be inserted.
     * @return true if the underlying collection was changed or false otherwise.
     * */
    public boolean push(T key) {
        if (this.length == 0) {
            this.vertebrae.get(0).add(key);
            this.cord.increaseLength(0);
            this.length += 1;
            return true;
        }
        int vertebraIndex = this.spineIndexOf(key);
        if (vertebraIndex >= this.vertebrae.size()) {
            vertebraIndex -= 1;
        }
        boolean extended = this.vertebrae.get(vertebraIndex).add(key);
        if (!extended) {
            return false;
        }
        if (this.vertebrae.get(vertebraIndex).size() > this.vertebraSize) {
            this.balance(vertebraIndex);
        } else {
            this.cord.increaseLength(vertebraIndex);
        }
        this.length += 1;
        return true;
    }

    /* Deletes a key ensuring sortedness.
     * @param key the key to be removed.
     * @return true if the underlying collection was changed or false otherwise.
     * */
    public boolean delete(T key) {
        if (this.length == 0) {
            return false;
        }
        int vertebraIndex = this.spineIndexOf(key);
        if (vertebraIndex >= this.vertebrae.size()) {
            vertebraIndex -= 1;
        }
        boolean shrink = this.vertebrae.get(vertebraIndex).delete(key);
        if (!shrink) {
            return false;
        }
        this.cord.decreaseLength(vertebraIndex);
        if (this.vertebrae.get(vertebraIndex).size() == 0) {
            if (this.length != 1) {
                this.vertebrae.remove(vertebraIndex);
            }
            this.cord = new Cord(this.vertebrae);
        }
        this.length -= 1;
        return true;
    }

    private IntegerTuple locate(int ith) {
        if (ith >= this.length || ith < 0) {
            return new IntegerTuple(-1,-1);
        }
        int vertebraIndex = this.cord.indexOf(ith);
        int offset = 0;
        if (vertebraIndex != 0) {
            offset = this.cord.prefixSum(vertebraIndex);
        }
        return new IntegerTuple(vertebraIndex, ith - offset);
    }

    /* Deletes the ith element.
     * @param ith
     * @return the deleted element, null otherwise.
     * */
    public T deleteByIndex(int ith) {
        if (this.length == 0 || ith >= this.length) {
            return null;
        }
        IntegerTuple indexes = this.locate(ith);
        int spineIndex = indexes.k;
        int vertebraIndex = indexes.v;
        if (spineIndex == -1 || vertebraIndex == -1) {
            return null;
        }
        T out = this.vertebrae.get(spineIndex).get(vertebraIndex);
        this.vertebrae.get(spineIndex).remove(vertebraIndex);
        this.cord.decreaseLength(vertebraIndex);
        if (this.vertebrae.get(spineIndex).size() == 0) {
            if (this.length != 1) {
                this.vertebrae.remove(spineIndex);
            }
            this.cord = new Cord(this.vertebrae);
        }
        this.length -= 1;
        return out;
    }

    /* Gets the ith element.
     * @param ith
     * @return the ith element, null otherwise.
     * */
    public T get(int ith) {
        if (this.length == 0 || ith >= this.length) {
            return null;
        }
        IntegerTuple indexes = this.locate(ith);
        int spineIndex = indexes.k;
        int vertebraIndex = indexes.v;
        return this.vertebrae.get(spineIndex).get(vertebraIndex);
    }

    /* Asserts the existence of some key.
     * @param key the key whose existence within the container is in question.
     * @return true if it exists, false otherwise.
     * */
    public boolean has(T key) {
        if (this.length == 0) {
            return false;
        }
        int vertebraIndex = this.spineIndexOf(key);
        if (vertebraIndex >= this.vertebrae.size()) {
            return false;
        }
        return this.vertebrae.get(vertebraIndex).has(key);
    }

    /* Removes and returns the smallest element
     * @return the element in question or null.
     * */
    public T pollFirst() {
        if (this.length == 0) {
            return null;
        }
        T out = this.vertebrae.get(0).remove(0);
        this.cord.decreaseLength(0);
        if (this.vertebrae.get(0).size() == 0) {
            if (this.length != 1) {
                this.vertebrae.remove(0);
            }
            this.cord = new Cord(this.vertebrae);
        }
        this.length -= 1;
        return out;
    }

    /* Returns the largest element
     * @return the element in question or null
     * */
    public T peekFirst() {
        if (this.length == 0) {
            return null;
        }
        return this.vertebrae.get(0).get(0);
    }

    /* Removes and returns the largest element
     * @return the element in question or null
     * */
    public T pollLast() {
        if (this.length == 0) {
            return null;
        }
        int vertebraeSize = vertebrae.size();
        int lastVertebraSize = this.vertebrae.get(vertebraeSize - 1).size();
        T out = this.vertebrae.get(vertebraeSize - 1).remove(lastVertebraSize - 1);
        this.cord.decreaseLength(vertebraeSize - 1);
        if (this.vertebrae.get(vertebraeSize - 1).size() == 0) {
            if (this.length != 1) {
                this.vertebrae.remove(vertebraeSize - 1);
            }
            this.cord = new Cord(this.vertebrae);
        } else {
            this.vertebrae.get(vertebraeSize - 1).max = this.vertebrae.get(vertebraeSize - 1).get(lastVertebraSize - 2);
        }
        this.length -= 1;
        return out;
    }

    public T peekLast() {
        if (this.length == 0) {
            return null;
        }
        int vertebraeSize = vertebrae.size();
        int lastVertebraSize = this.vertebrae.get(vertebraeSize - 1).size();
        return this.vertebrae.get(vertebraeSize - 1).get(lastVertebraSize - 1);
    }
}