package net.minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VillageCollection extends WorldSavedData {
    private World worldObj;
    public final List<ChunkCoordinates> a;
    private final List<VillageDoorInfo> d;
    public final List<Village> b;
    private int tickCounter;

    public VillageCollection(String string) {
        super(string);
        this.a = new ArrayList();
        this.d = new ArrayList();
        this.b = new ArrayList();
    }

    public VillageCollection(World world) {
        super("villages");
        this.a = new ArrayList();
        this.d = new ArrayList();
        this.b = new ArrayList();
        this.worldObj = world;
        b();
    }

    public final void func_82566_a(World world) {
        this.worldObj = world;
        Iterator<Village> it = this.b.iterator();
        while (it.hasNext()) {
            it.next().worldObj = world;
        }
    }

    public final void a() {
        VillageDoorInfo villageDoorInfo;
        VillageDoorInfo villageDoorInfo2;
        this.tickCounter++;
        Iterator<Village> it = this.b.iterator();
        while (it.hasNext()) {
            it.next().a(this.tickCounter);
        }
        Iterator<Village> it2 = this.b.iterator();
        while (it2.hasNext()) {
            if (it2.next().b.isEmpty()) {
                it2.remove();
                b();
            }
        }
        if (!this.a.isEmpty()) {
            ChunkCoordinates chunkCoordinatesRemove = this.a.remove(0);
            for (int i = chunkCoordinatesRemove.posX - 16; i < chunkCoordinatesRemove.posX + 16; i++) {
                for (int i2 = chunkCoordinatesRemove.posY - 4; i2 < chunkCoordinatesRemove.posY + 4; i2++) {
                    for (int i3 = chunkCoordinatesRemove.posZ - 16; i3 < chunkCoordinatesRemove.posZ + 16; i3++) {
                        if (this.worldObj.getBlockId(i, i2, i3) == Block.doorWood.blockID) {
                            int i4 = i;
                            int i5 = i2;
                            int i6 = i3;
                            Iterator<VillageDoorInfo> it3 = this.d.iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    Iterator<Village> it4 = this.b.iterator();
                                    while (true) {
                                        if (!it4.hasNext()) {
                                            villageDoorInfo = null;
                                            break;
                                        }
                                        Village next = it4.next();
                                        if (next.center.getDistanceSquared(i4, i5, i6) > next.e * next.e) {
                                            villageDoorInfo2 = null;
                                        } else {
                                            Iterator<VillageDoorInfo> it5 = next.b.iterator();
                                            while (true) {
                                                if (!it5.hasNext()) {
                                                    villageDoorInfo2 = null;
                                                    break;
                                                }
                                                VillageDoorInfo next2 = it5.next();
                                                if (next2.posX == i4 && next2.posZ == i6 && Math.abs(next2.posY - i5) <= 1) {
                                                    villageDoorInfo2 = next2;
                                                    break;
                                                }
                                            }
                                        }
                                        VillageDoorInfo villageDoorInfo3 = villageDoorInfo2;
                                        if (villageDoorInfo2 != null) {
                                            villageDoorInfo = villageDoorInfo3;
                                            break;
                                        }
                                    }
                                } else {
                                    VillageDoorInfo next3 = it3.next();
                                    if (next3.posX == i4 && next3.posZ == i6 && Math.abs(next3.posY - i5) <= 1) {
                                        villageDoorInfo = next3;
                                        break;
                                    }
                                }
                            }
                            VillageDoorInfo villageDoorInfo4 = villageDoorInfo;
                            if (villageDoorInfo != null) {
                                villageDoorInfo4.lastActivityTimestamp = this.tickCounter;
                            } else {
                                int i7 = i;
                                int i8 = i2;
                                int i9 = i3;
                                BlockDoor blockDoor = Block.doorWood;
                                int fullMetadata = BlockDoor.getFullMetadata(this.worldObj, i7, i8, i9) & 3;
                                if (fullMetadata != 0 && fullMetadata != 2) {
                                    int i10 = 0;
                                    for (int i11 = -5; i11 < 0; i11++) {
                                        if (this.worldObj.canBlockSeeTheSky(i7, i8, i9 + i11)) {
                                            i10--;
                                        }
                                    }
                                    for (int i12 = 1; i12 <= 5; i12++) {
                                        if (this.worldObj.canBlockSeeTheSky(i7, i8, i9 + i12)) {
                                            i10++;
                                        }
                                    }
                                    if (i10 != 0) {
                                        this.d.add(new VillageDoorInfo(i7, i8, i9, 0, i10 > 0 ? -2 : 2, this.tickCounter));
                                    }
                                } else {
                                    int i13 = 0;
                                    for (int i14 = -5; i14 < 0; i14++) {
                                        if (this.worldObj.canBlockSeeTheSky(i7 + i14, i8, i9)) {
                                            i13--;
                                        }
                                    }
                                    for (int i15 = 1; i15 <= 5; i15++) {
                                        if (this.worldObj.canBlockSeeTheSky(i7 + i15, i8, i9)) {
                                            i13++;
                                        }
                                    }
                                    if (i13 != 0) {
                                        this.d.add(new VillageDoorInfo(i7, i8, i9, i13 > 0 ? -2 : 2, 0, this.tickCounter));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i16 = 0; i16 < this.d.size(); i16++) {
            VillageDoorInfo villageDoorInfo5 = this.d.get(i16);
            boolean z = false;
            Iterator<Village> it6 = this.b.iterator();
            while (true) {
                if (!it6.hasNext()) {
                    break;
                }
                Village next4 = it6.next();
                int distanceSquared = (int) next4.center.getDistanceSquared(villageDoorInfo5.posX, villageDoorInfo5.posY, villageDoorInfo5.posZ);
                int i17 = 32 + next4.e;
                if (distanceSquared <= i17 * i17) {
                    next4.addVillageDoorInfo(villageDoorInfo5);
                    z = true;
                    break;
                }
            }
            if (!z) {
                Village village = new Village(this.worldObj);
                village.addVillageDoorInfo(villageDoorInfo5);
                this.b.add(village);
                b();
            }
        }
        this.d.clear();
        if (this.tickCounter % 400 == 0) {
            b();
        }
    }

    public final Village findNearestVillage(int i, int i2, int i3, int i4) {
        Village village = null;
        float f = Float.MAX_VALUE;
        for (Village village2 : this.b) {
            float distanceSquared = village2.center.getDistanceSquared(i, i2, i3);
            if (distanceSquared < f) {
                float f2 = i4 + village2.e;
                if (distanceSquared <= f2 * f2) {
                    village = village2;
                    f = distanceSquared;
                }
            }
        }
        return village;
    }

    @Override // net.minecraft.WorldSavedData
    public final void readFromNBT(NBTTagCompound nBTTagCompound) {
        this.tickCounter = nBTTagCompound.getInteger("Tick");
        NBTTagList tagList = nBTTagCompound.getTagList("Villages");
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound nBTTagCompound2 = (NBTTagCompound) tagList.tagAt(i);
            Village village = new Village();
            village.h = nBTTagCompound2.getInteger("PopSize");
            village.e = nBTTagCompound2.getInteger("Radius");
            village.numIronGolems = nBTTagCompound2.getInteger("Golems");
            village.f = nBTTagCompound2.getInteger("Stable");
            village.g = nBTTagCompound2.getInteger("Tick");
            village.i = nBTTagCompound2.getInteger("MTick");
            village.center.posX = nBTTagCompound2.getInteger("CX");
            village.center.posY = nBTTagCompound2.getInteger("CY");
            village.center.posZ = nBTTagCompound2.getInteger("CZ");
            village.centerHelper.posX = nBTTagCompound2.getInteger("ACX");
            village.centerHelper.posY = nBTTagCompound2.getInteger("ACY");
            village.centerHelper.posZ = nBTTagCompound2.getInteger("ACZ");
            NBTTagList tagList2 = nBTTagCompound2.getTagList("Doors");
            for (int i2 = 0; i2 < tagList2.tagCount(); i2++) {
                NBTTagCompound nBTTagCompound3 = (NBTTagCompound) tagList2.tagAt(i2);
                village.b.add(new VillageDoorInfo(nBTTagCompound3.getInteger("X"), nBTTagCompound3.getInteger("Y"), nBTTagCompound3.getInteger("Z"), nBTTagCompound3.getInteger("IDX"), nBTTagCompound3.getInteger("IDZ"), nBTTagCompound3.getInteger("TS")));
            }
            NBTTagList tagList3 = nBTTagCompound2.getTagList("Players");
            for (int i3 = 0; i3 < tagList3.tagCount(); i3++) {
                NBTTagCompound nBTTagCompound4 = (NBTTagCompound) tagList3.tagAt(i3);
                village.j.put(nBTTagCompound4.getString("Name"), Integer.valueOf(nBTTagCompound4.getInteger("S")));
            }
            this.b.add(village);
        }
    }

    @Override
    public final void writeToNBT(NBTTagCompound nBTTagCompound) {
        nBTTagCompound.setInteger("Tick", this.tickCounter);
        NBTTagList nBTTagList = new NBTTagList("Villages");
        for (Village village : this.b) {
            NBTTagCompound nBTTagCompound2 = new NBTTagCompound("Village");
            nBTTagCompound2.setInteger("PopSize", village.h);
            nBTTagCompound2.setInteger("Radius", village.e);
            nBTTagCompound2.setInteger("Golems", village.numIronGolems);
            nBTTagCompound2.setInteger("Stable", village.f);
            nBTTagCompound2.setInteger("Tick", village.g);
            nBTTagCompound2.setInteger("MTick", village.i);
            nBTTagCompound2.setInteger("CX", village.center.posX);
            nBTTagCompound2.setInteger("CY", village.center.posY);
            nBTTagCompound2.setInteger("CZ", village.center.posZ);
            nBTTagCompound2.setInteger("ACX", village.centerHelper.posX);
            nBTTagCompound2.setInteger("ACY", village.centerHelper.posY);
            nBTTagCompound2.setInteger("ACZ", village.centerHelper.posZ);
            NBTTagList nBTTagList2 = new NBTTagList("Doors");
            for (VillageDoorInfo villageDoorInfo : village.b) {
                NBTTagCompound nBTTagCompound3 = new NBTTagCompound("Door");
                nBTTagCompound3.setInteger("X", villageDoorInfo.posX);
                nBTTagCompound3.setInteger("Y", villageDoorInfo.posY);
                nBTTagCompound3.setInteger("Z", villageDoorInfo.posZ);
                nBTTagCompound3.setInteger("IDX", villageDoorInfo.insideDirectionX);
                nBTTagCompound3.setInteger("IDZ", villageDoorInfo.insideDirectionZ);
                nBTTagCompound3.setInteger("TS", villageDoorInfo.lastActivityTimestamp);
                nBTTagList2.appendTag(nBTTagCompound3);
            }
            nBTTagCompound2.setTag("Doors", nBTTagList2);
            NBTTagList nBTTagList3 = new NBTTagList("Players");
            for (String str : village.j.keySet()) {
                NBTTagCompound nBTTagCompound4 = new NBTTagCompound(str);
                nBTTagCompound4.setString("Name", str);
                nBTTagCompound4.setInteger("S", village.j.get(str).intValue());
                nBTTagList3.appendTag(nBTTagCompound4);
            }
            nBTTagCompound2.setTag("Players", nBTTagList3);
            nBTTagList.appendTag(nBTTagCompound2);
        }
        nBTTagCompound.setTag("Villages", nBTTagList);
    }
}